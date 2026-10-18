package com.financaspro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financaspro.model.dto.CotacaoDetalheDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CotacaoMercadoService {

    private static final Pattern B3_TICKER_PATTERN = Pattern.compile("^[A-Z]{4}(?:3|4|5|6|11|12|34|35)[B]?$", Pattern.CASE_INSENSITIVE);
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutos de cache em memória
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();

    @Value("${brapi.token:${BRAPI_TOKEN:}}")
    private String brapiToken;

    public static class CacheItem {
        final BigDecimal preco;
        final String provedor;
        final Instant timestamp;

        public CacheItem(BigDecimal preco, String provedor) {
            this.preco = preco;
            this.provedor = provedor;
            this.timestamp = Instant.now();
        }

        public boolean isValido() {
            return Instant.now().minusSeconds(CACHE_TTL_SECONDS).isBefore(timestamp);
        }

        public BigDecimal getPreco() {
            return preco;
        }

        public String getProvedor() {
            return provedor;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    public BigDecimal obterCotacaoAtual(String ticker) {
        CotacaoDetalheDTO detalhe = obterDetalhesCotacao(ticker);
        return detalhe != null ? detalhe.getPrecoAtual() : null;
    }

    public CotacaoDetalheDTO obterDetalhesCotacao(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return null;
        }

        String t = ticker.toUpperCase().trim();

        // Não consulta internet para ativos sintéticos ou de Renda Fixa (ex: CDB_BTG_CDI, TESOURO_SELIC)
        if (!isTickerB3Valido(t)) {
            log.debug("Ticker {} não corresponde ao padrão de ativos cotados na B3 (Ação/FII/BDR). Consulta ignorada.", t);
            return null;
        }

        CacheItem itemCache = cache.get(t);
        if (itemCache != null && itemCache.isValido()) {
            return CotacaoDetalheDTO.builder()
                    .ticker(t)
                    .precoAtual(itemCache.preco)
                    .provedor(itemCache.provedor + " (Cache)")
                    .emTempoReal(true)
                    .atualizadoEm(itemCache.timestamp)
                    .mensagem("Cotação recuperada do cache local")
                    .build();
        }

        // 1ª Tentativa: Yahoo Finance Primário (query1)
        BigDecimal preco = buscarCotacaoYahoo(t, "https://query1.finance.yahoo.com");
        String provedor = "Yahoo Finance (query1)";

        // 2ª Tentativa: Yahoo Finance Secundário (query2)
        if (preco == null) {
            preco = buscarCotacaoYahoo(t, "https://query2.finance.yahoo.com");
            provedor = "Yahoo Finance (query2)";
        }

        // 3ª Tentativa: BRAPI (se token configurado ou endpoint aberto)
        if (preco == null) {
            preco = buscarCotacaoBrapi(t);
            provedor = "BRAPI Mercado";
        }

        if (preco != null && preco.compareTo(BigDecimal.ZERO) > 0) {
            cache.put(t, new CacheItem(preco, provedor));
            log.info("Cotação B3 obtida com sucesso na internet para {}: R$ {} via {}", t, preco, provedor);
            return CotacaoDetalheDTO.builder()
                    .ticker(t)
                    .precoAtual(preco)
                    .provedor(provedor)
                    .emTempoReal(true)
                    .atualizadoEm(Instant.now())
                    .mensagem("Cotação obtida online com sucesso")
                    .build();
        }

        // Se falhou agora mas tínhamos em cache antigo, usa como fallback de resiliência
        if (itemCache != null) {
            log.warn("Falha ao atualizar cotação para {}. Utilizando valor anterior em cache: R$ {}", t, itemCache.preco);
            return CotacaoDetalheDTO.builder()
                    .ticker(t)
                    .precoAtual(itemCache.preco)
                    .provedor(itemCache.provedor + " (Expirado)")
                    .emTempoReal(false)
                    .atualizadoEm(itemCache.timestamp)
                    .mensagem("Cotação obtida do cache anterior")
                    .build();
        }

        log.warn("Nenhum provedor conseguiu fornecer cotação atualizada para o ticker B3: {}", t);
        return null;
    }

    public boolean isTickerB3Valido(String ticker) {
        if (ticker == null) return false;
        String t = ticker.trim().toUpperCase();
        if (t.endsWith(".SA")) {
            t = t.substring(0, t.length() - 3);
        }
        return B3_TICKER_PATTERN.matcher(t).matches();
    }

    private BigDecimal buscarCotacaoYahoo(String ticker, String baseUrl) {
        String tickerYahoo = ticker.endsWith(".SA") ? ticker : ticker + ".SA";
        String urlStr = baseUrl + "/v8/finance/chart/" + tickerYahoo;

        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            int status = conn.getResponseCode();
            if (status == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    JsonNode result = root.path("chart").path("result");
                    if (result.isArray() && result.size() > 0) {
                        JsonNode meta = result.get(0).path("meta");
                        BigDecimal preco = extrairPrecoValido(meta);
                        if (preco != null && preco.compareTo(BigDecimal.ZERO) > 0) {
                            return preco.setScale(2, RoundingMode.HALF_UP);
                        }
                    }
                }
            } else {
                log.debug("Resposta HTTP {} de {} para {}", status, baseUrl, ticker);
            }
        } catch (Exception e) {
            log.debug("Erro ao consultar cotação em {} para {}: {}", baseUrl, ticker, e.getMessage());
        }

        return null;
    }

    private BigDecimal extrairPrecoValido(JsonNode meta) {
        if (meta == null) return null;

        // 1. regularMarketPrice
        if (meta.has("regularMarketPrice") && !meta.get("regularMarketPrice").isNull()) {
            double p = meta.get("regularMarketPrice").asDouble();
            if (p > 0) return BigDecimal.valueOf(p);
        }

        // 2. chartPreviousClose
        if (meta.has("chartPreviousClose") && !meta.get("chartPreviousClose").isNull()) {
            double p = meta.get("chartPreviousClose").asDouble();
            if (p > 0) return BigDecimal.valueOf(p);
        }

        // 3. previousClose
        if (meta.has("previousClose") && !meta.get("previousClose").isNull()) {
            double p = meta.get("previousClose").asDouble();
            if (p > 0) return BigDecimal.valueOf(p);
        }

        // 4. fulldayPrice
        if (meta.has("fulldayPrice") && !meta.get("fulldayPrice").isNull()) {
            double p = meta.get("fulldayPrice").asDouble();
            if (p > 0) return BigDecimal.valueOf(p);
        }

        return null;
    }

    private BigDecimal buscarCotacaoBrapi(String ticker) {
        String cleanTicker = ticker.endsWith(".SA") ? ticker.substring(0, ticker.length() - 3) : ticker;
        String urlStr = "https://brapi.dev/api/quote/" + cleanTicker;
        if (brapiToken != null && !brapiToken.trim().isEmpty()) {
            urlStr += "?token=" + brapiToken.trim();
        }

        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            int status = conn.getResponseCode();
            if (status == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    JsonNode results = root.path("results");
                    if (results.isArray() && results.size() > 0) {
                        JsonNode item = results.get(0);
                        if (item.has("regularMarketPrice") && !item.get("regularMarketPrice").isNull()) {
                            double p = item.get("regularMarketPrice").asDouble();
                            if (p > 0) {
                                return BigDecimal.valueOf(p).setScale(2, RoundingMode.HALF_UP);
                            }
                        }
                    }
                }
            } else {
                log.debug("Resposta HTTP {} de BRAPI para {}", status, cleanTicker);
            }
        } catch (Exception e) {
            log.debug("Erro ao consultar cotação BRAPI para {}: {}", cleanTicker, e.getMessage());
        }

        return null;
    }

    public void limparCache() {
        cache.clear();
        log.info("Cache de cotações de mercado limpo com sucesso.");
    }
}
