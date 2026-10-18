package com.financaspro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

@Service
@Slf4j
public class CotacaoMercadoService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_SECONDS = 300; // 5 minutos de cache

    private static class CacheItem {
        final BigDecimal preco;
        final Instant timestamp;

        CacheItem(BigDecimal preco) {
            this.preco = preco;
            this.timestamp = Instant.now();
        }

        boolean isValido() {
            return Instant.now().minusSeconds(CACHE_TTL_SECONDS).isBefore(timestamp);
        }
    }

    public BigDecimal obterCotacaoAtual(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            return null;
        }

        String t = ticker.toUpperCase().trim();
        CacheItem item = cache.get(t);
        if (item != null && item.isValido()) {
            return item.preco;
        }

        BigDecimal precoBuscado = buscarCotacaoYahoo(t);
        if (precoBuscado != null && precoBuscado.compareTo(BigDecimal.ZERO) > 0) {
            cache.put(t, new CacheItem(precoBuscado));
            return precoBuscado;
        }

        if (item != null) {
            return item.preco;
        }

        return null;
    }

    private BigDecimal buscarCotacaoYahoo(String ticker) {
        String tickerYahoo = ticker.endsWith(".SA") ? ticker : ticker + ".SA";
        String urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/" + tickerYahoo;

        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            int status = conn.getResponseCode();
            if (status == 200) {
                try (InputStream is = conn.getInputStream()) {
                    JsonNode root = objectMapper.readTree(is);
                    JsonNode result = root.path("chart").path("result");
                    if (result.isArray() && result.size() > 0) {
                        JsonNode meta = result.get(0).path("meta");
                        if (meta.has("regularMarketPrice")) {
                            double price = meta.get("regularMarketPrice").asDouble();
                            if (price > 0) {
                                log.info("Cotação obtida com sucesso via Yahoo Finance para {}: R$ {}", ticker, price);
                                return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
                            }
                        }
                    }
                }
            } else {
                log.warn("Falha ao consultar Yahoo Finance para {}: HTTP {}", ticker, status);
            }
        } catch (Exception e) {
            log.warn("Erro ao consultar cotação Yahoo Finance para {}: {}", ticker, e.getMessage());
        }

        return null;
    }
}
