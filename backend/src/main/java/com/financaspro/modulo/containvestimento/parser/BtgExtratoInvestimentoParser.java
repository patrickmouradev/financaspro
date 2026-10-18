package com.financaspro.modulo.containvestimento.parser;

import com.financaspro.model.dto.AtivoDetectadoDTO;
import com.financaspro.utils.CsvExcelParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BtgExtratoInvestimentoParser {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_BR_2D = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter FORMATO_DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_HORA_BR_2D = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    private static final Pattern PATTERN_DATA_INICIO = Pattern.compile("^(\\d{2}/\\d{2}/\\d{2,4})\\b");
    private static final Pattern PATTERN_VALOR = Pattern.compile("(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
    private static final Pattern PATTERN_TICKER = Pattern.compile("\\b([A-Z]{4}[0-9]{1,2}[B]?)\\b");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemExtratoInvestimento {
        private LocalDateTime dataMovimento;
        private String descricao;
        private String tipo;
        private String ticker;
        private BigDecimal valor;
        private BigDecimal saldoResultante;
        private String origem;
        private String observacao;
    }

    private static class TradeInfo {
        String dataStr;
        String tipoOp;
        String ticker;
        BigDecimal qtd;
        BigDecimal preco;
        BigDecimal valorTotal;
    }

    public boolean ehFormatoBtgInvestimento(byte[] arquivoBytes, String nomeArquivo, String senha) {
        if (nomeArquivo != null) {
            String upper = nomeArquivo.toUpperCase();
            if (upper.contains("INVEST") || upper.contains("AUVP")) {
                return true;
            }
        }
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            for (List<String> colunas : linhas) {
                String linhaTexto = limparTexto(String.join(" ", colunas)).toLowerCase();
                if (linhaTexto.contains("extrato de conta investimento")
                        || (linhaTexto.contains("extrato") && linhaTexto.contains("investimento"))
                        || (linhaTexto.contains("cont") && linhaTexto.contains("investimento"))
                        || (linhaTexto.contains("cont") && linhaTexto.contains("corrente - moviment"))
                        || linhaTexto.contains("saldo conta investimentos")
                        || (linhaTexto.contains("saldo cont") && linhaTexto.contains("investimentos"))
                        || linhaTexto.contains("auvp capital")
                        || linhaTexto.contains("auvp consultoria")
                        || (linhaTexto.contains("btg pactual") && (linhaTexto.contains("carteira") || linhaTexto.contains("renda fixa") || linhaTexto.contains("renda vari")))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<ItemExtratoInvestimento> parsearExtratoInvestimento(byte[] arquivoBytes, String senha) throws Exception {
        List<ItemExtratoInvestimento> itens = new ArrayList<>();
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
        if (linhas == null || linhas.isEmpty()) {
            return itens;
        }

        // 1. Extrair trades de Renda Variável (para enriquecer LIQ BOLSA com ticker, operacao e preco)
        Map<String, TradeInfo> tradesMap = extrairTradesRendaVariavel(linhas);

        // 2. Extrair movimentações de Conta Corrente / Saldo de Investimento (PDF BTG / AUVP)
        boolean achouSecaoConta = extrairMovimentacoesConta(linhas, itens, tradesMap);

        // 3. Fallback: modo tabular (para arquivos Excel / CSV com colunas estruturadas)
        if (itens.isEmpty() && !achouSecaoConta) {
            extrairModoTabular(linhas, itens);
        }

        return itens;
    }

    private Map<String, TradeInfo> extrairTradesRendaVariavel(List<List<String>> linhas) {
        Map<String, TradeInfo> trades = new HashMap<>();
        boolean inRvMov = false;

        for (int i = 0; i < linhas.size(); i++) {
            List<String> colunas = linhas.get(i);
            String linhaTexto = limparTexto(String.join(" ", colunas));
            String upper = linhaTexto.toUpperCase();

            if (upper.contains("REND") && upper.contains("VARI") && upper.contains("MOVIMENTA")) {
                inRvMov = true;
                continue;
            }
            if (inRvMov && (upper.contains("CONTA CORRENTE") || upper.contains("POSI") || upper.contains("VALORES EM TR") || upper.contains("DISCLAIMERS"))) {
                inRvMov = false;
                continue;
            }

            if (inRvMov) {
                Matcher mData = PATTERN_DATA_INICIO.matcher(linhaTexto);
                if (mData.find()) {
                    String dataStr = mData.group(1);
                    String combined = linhaTexto;
                    String linhaNumeros = linhaTexto;

                    if (i + 1 < linhas.size()) {
                        String prox = limparTexto(String.join(" ", linhas.get(i + 1)));
                        if (!PATTERN_DATA_INICIO.matcher(prox).find() && !prox.toUpperCase().contains("TOTAL")) {
                            combined = linhaTexto + " " + prox;
                        }
                    }

                    Matcher mTrade = Pattern.compile("\\b(COMPRA|VENDA)\\s+([A-Z0-9]{4,6})", Pattern.CASE_INSENSITIVE).matcher(combined);
                    if (mTrade.find()) {
                        String op = mTrade.group(1).toUpperCase();
                        String ticker = mTrade.group(2).toUpperCase();

                        TradeInfo info = new TradeInfo();
                        info.dataStr = dataStr;
                        info.tipoOp = op;
                        info.ticker = ticker;

                        String apenasNums = linhaNumeros.substring(mData.end()).trim();
                        String[] partes = apenasNums.split("\\s+");
                        if (partes.length >= 1) {
                            try {
                                info.qtd = new BigDecimal(partes[0].replace(".", "").replace(",", "."));
                            } catch (Exception ignored) {}
                        }
                        if (partes.length >= 2) {
                            try {
                                info.preco = parseBigDecimal(partes[1]);
                            } catch (Exception ignored) {}
                        }
                        if (partes.length >= 3) {
                            try {
                                info.valorTotal = parseBigDecimal(partes[2]);
                            } catch (Exception ignored) {}
                        }

                        trades.put(dataStr, info);
                        trades.put(dataStr.replace("/26", "/2026"), info);
                        trades.put(ticker, info);
                    }
                }
            }
        }
        return trades;
    }

    private boolean extrairMovimentacoesConta(List<List<String>> linhas,
                                             List<ItemExtratoInvestimento> itens,
                                             Map<String, TradeInfo> tradesMap) {
        boolean inCcMov = false;
        boolean encontrouSecao = false;

        for (int i = 0; i < linhas.size(); i++) {
            List<String> cols = linhas.get(i);
            String linhaTexto = limparTexto(String.join(" ", cols));
            String upper = linhaTexto.toUpperCase();

            if (upper.contains("CONT") && upper.contains("CORRENTE") && upper.contains("MOVIMENTA")) {
                inCcMov = true;
                encontrouSecao = true;
                continue;
            }

            if (inCcMov) {
                if (upper.contains("TOTAL DE") || upper.contains("VALORES EM TR") || upper.contains("PERFIL DE RISCO") || upper.contains("DISCLAIMERS")) {
                    inCcMov = false;
                    break;
                }

                Matcher mData = PATTERN_DATA_INICIO.matcher(linhaTexto);
                if (mData.find()) {
                    String dataStr = mData.group(1);
                    LocalDateTime dataHora = extrairDataHora(dataStr);
                    if (dataHora == null) {
                        continue;
                    }

                    String resto = linhaTexto.substring(mData.end()).trim();
                    if (resto.toUpperCase().contains("SALDO ANTERIOR") || resto.toUpperCase().contains("SALDO INICIAL")) {
                        continue;
                    }

                    List<String> valoresEncontrados = new ArrayList<>();
                    Matcher mVal = PATTERN_VALOR.matcher(resto);
                    while (mVal.find()) {
                        valoresEncontrados.add(mVal.group(1));
                    }

                    String descricao = "";
                    BigDecimal valorMov = null;
                    BigDecimal saldoResultante = null;

                    if (valoresEncontrados.size() >= 2) {
                        valorMov = parseBigDecimal(valoresEncontrados.get(0));
                        saldoResultante = parseBigDecimal(valoresEncontrados.get(1));
                        String descCandidate = PATTERN_VALOR.matcher(resto).replaceAll("").trim();
                        if (descCandidate.length() >= 3) {
                            descricao = descCandidate;
                        } else if (i + 1 < linhas.size()) {
                            String prox = limparTexto(String.join(" ", linhas.get(i + 1)));
                            if (!PATTERN_DATA_INICIO.matcher(prox).find() && !prox.toUpperCase().contains("TOTAL")) {
                                i++;
                                descricao = prox;
                            }
                        }
                    } else if (valoresEncontrados.size() == 1) {
                        valorMov = parseBigDecimal(valoresEncontrados.get(0));
                        String descCandidate = PATTERN_VALOR.matcher(resto).replaceAll("").trim();
                        if (descCandidate.length() >= 3) {
                            descricao = descCandidate;
                        } else if (i + 1 < linhas.size()) {
                            String prox = limparTexto(String.join(" ", linhas.get(i + 1)));
                            if (!PATTERN_DATA_INICIO.matcher(prox).find() && !prox.toUpperCase().contains("TOTAL")) {
                                i++;
                                descricao = prox;
                            }
                        }
                    } else {
                        if (i + 1 < linhas.size()) {
                            String prox = limparTexto(String.join(" ", linhas.get(i + 1)));
                            Matcher mValProx = PATTERN_VALOR.matcher(prox);
                            List<String> valProxList = new ArrayList<>();
                            while (mValProx.find()) {
                                valProxList.add(mValProx.group(1));
                            }
                            if (!valProxList.isEmpty()) {
                                valorMov = parseBigDecimal(valProxList.get(0));
                                if (valProxList.size() >= 2) {
                                    saldoResultante = parseBigDecimal(valProxList.get(1));
                                }
                                descricao = resto;
                                i++;
                            }
                        }
                    }

                    if (valorMov != null && !descricao.isEmpty()) {
                        String descFinal = descricao;
                        String ticker = extrairTicker(descFinal);
                        String tipoLancamento = mapearTipoInvestimento(descFinal);

                        if (descFinal.toUpperCase().contains("LIQ BOLSA")) {
                            tipoLancamento = (valorMov.compareTo(BigDecimal.ZERO) < 0) ? "COMPRA_ATIVO" : "VENDA_ATIVO";
                            TradeInfo tradeMatch = null;
                            for (Map.Entry<String, TradeInfo> entry : tradesMap.entrySet()) {
                                if (descFinal.contains(entry.getKey())) {
                                    tradeMatch = entry.getValue();
                                    break;
                                }
                            }
                            if (tradeMatch == null && !tradesMap.isEmpty()) {
                                tradeMatch = tradesMap.values().iterator().next();
                            }

                            if (tradeMatch != null) {
                                ticker = tradeMatch.ticker;
                                String qtdStr = (tradeMatch.qtd != null) ? tradeMatch.qtd.stripTrailingZeros().toPlainString() : "1";
                                String precoStr = (tradeMatch.preco != null) ? " @ R$ " + tradeMatch.preco : "";
                                descFinal = "LIQ BOLSA - " + tradeMatch.tipoOp + " " + tradeMatch.ticker + " (Qtd: " + qtdStr + precoStr + ") [" + descFinal + "]";
                            }
                        }

                        itens.add(ItemExtratoInvestimento.builder()
                                .dataMovimento(dataHora)
                                .descricao(descFinal)
                                .tipo(tipoLancamento)
                                .ticker(ticker)
                                .valor(valorMov)
                                .saldoResultante(saldoResultante)
                                .origem("BTG_INVESTIMENTO")
                                .observacao("Extrato Conta Investimento AUVP / BTG Pactual")
                                .build());
                    }
                }
            }
        }

        return encontrouSecao;
    }

    private void extrairModoTabular(List<List<String>> linhas, List<ItemExtratoInvestimento> itens) {
        int colData = -1;
        int colDesc = -1;
        int colDebito = -1;
        int colCredito = -1;
        int colMov = -1;
        int colSaldo = -1;

        for (List<String> linha : linhas) {
            if (linha == null || linha.isEmpty()) continue;

            if (colData == -1 || (colMov == -1 && colDebito == -1 && colCredito == -1)) {
                for (int c = 0; c < linha.size(); c++) {
                    String cel = limparTexto(linha.get(c)).toLowerCase();
                    if (cel.contains("data") || cel.contains("dat")) colData = c;
                    else if (cel.contains("descri") || cel.contains("hist")) colDesc = c;
                    else if (cel.contains("movimenta")) colMov = c;
                    else if (cel.contains("d b") || cel.contains("debito")) colDebito = c;
                    else if (cel.contains("cr ") || cel.contains("credito")) colCredito = c;
                    else if (cel.contains("saldo")) colSaldo = c;
                }
                if (colData != -1 && (colMov != -1 || colDebito != -1 || colCredito != -1)) {
                    continue;
                }
            }

            if (colData == -1 || (colMov == -1 && colDebito == -1 && colCredito == -1)) continue;

            String dataStr = (colData < linha.size()) ? limparTexto(linha.get(colData)) : "";
            if (dataStr.isEmpty()) continue;

            String dataLower = dataStr.toLowerCase();
            if (dataLower.contains("data") || dataLower.contains("extrato") || dataLower.contains("saldo")
                    || dataLower.contains("cliente") || dataLower.contains("cpf")) continue;

            LocalDateTime dataHora = extrairDataHora(dataStr);
            if (dataHora == null) continue;

            String descricaoStr = (colDesc != -1 && colDesc < linha.size()) ? limparTexto(linha.get(colDesc)) : "";
            if (descricaoStr.isEmpty() || descricaoStr.equalsIgnoreCase("Saldo Inicial") || descricaoStr.equalsIgnoreCase("Saldo Final")) continue;

            BigDecimal debitoVal = (colDebito != -1 && colDebito < linha.size()) ? parseBigDecimal(linha.get(colDebito)) : null;
            BigDecimal creditoVal = (colCredito != -1 && colCredito < linha.size()) ? parseBigDecimal(linha.get(colCredito)) : null;
            BigDecimal movVal = (colMov != -1 && colMov < linha.size()) ? parseBigDecimal(linha.get(colMov)) : null;
            BigDecimal saldoVal = (colSaldo != -1 && colSaldo < linha.size()) ? parseBigDecimal(linha.get(colSaldo)) : null;

            BigDecimal valorFinal = null;
            if (movVal != null) {
                valorFinal = movVal;
            } else if (creditoVal != null && creditoVal.compareTo(BigDecimal.ZERO) > 0) {
                valorFinal = creditoVal;
            } else if (debitoVal != null && debitoVal.compareTo(BigDecimal.ZERO) > 0) {
                valorFinal = debitoVal.negate();
            } else if (debitoVal != null && debitoVal.compareTo(BigDecimal.ZERO) < 0) {
                valorFinal = debitoVal;
            }

            if (valorFinal == null) continue;

            String ticker = extrairTicker(descricaoStr);
            String tipo = mapearTipoInvestimento(descricaoStr);

            itens.add(ItemExtratoInvestimento.builder()
                    .dataMovimento(dataHora)
                    .descricao(descricaoStr)
                    .tipo(tipo)
                    .ticker(ticker)
                    .valor(valorFinal)
                    .saldoResultante(saldoVal)
                    .origem("BTG_INVESTIMENTO")
                    .observacao("Extrato Conta Investimento AUVP / BTG Pactual")
                    .build());
        }
    }

    private String limparTexto(String s) {
        if (s == null) return "";
        return s.replace("\u0000", " ").trim();
    }

    public BigDecimal parseBigDecimal(String valorStr) {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanVal = valorStr.replace("R$", "").replace(" ", "").trim();
            if (cleanVal.contains(",") && cleanVal.contains(".")) {
                cleanVal = cleanVal.replace(".", "").replace(",", ".");
            } else if (cleanVal.contains(",")) {
                cleanVal = cleanVal.replace(",", ".");
            }
            return new BigDecimal(cleanVal);
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime extrairDataHora(String textoData) {
        if (textoData == null || textoData.trim().isEmpty()) {
            return null;
        }
        String t = textoData.trim();
        try {
            return LocalDateTime.parse(t, FORMATO_DATA_HORA_BR);
        } catch (Exception e1) {
            try {
                return LocalDateTime.parse(t, FORMATO_DATA_HORA_BR_2D);
            } catch (Exception e2) {
                try {
                    LocalDate ld = LocalDate.parse(t, FORMATO_DATA_BR);
                    return ld.atStartOfDay();
                } catch (Exception e3) {
                    try {
                        LocalDate ld2 = LocalDate.parse(t, FORMATO_DATA_BR_2D);
                        return ld2.atStartOfDay();
                    } catch (Exception e4) {
                        return null;
                    }
                }
            }
        }
    }

    public String extrairTicker(String desc) {
        if (desc == null) return null;
        Matcher m = PATTERN_TICKER.matcher(desc.toUpperCase());
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public String mapearTipoInvestimento(String desc) {
        if (desc == null) return "OUTROS";
        String d = desc.toUpperCase();

        if (d.contains("RENDIMENTOS") || d.contains("DIVIDENDOS") || d.contains("PROVENTOS") || d.contains("JCP")) {
            return "DIVIDENDO";
        }
        if (d.contains("CUPOM") || d.contains("CONTA REMUNERADA") || d.contains("JUROS") || d.contains("SALDO REMUNERADO") || d.contains("CASHBACK")) {
            return "RENDIMENTO";
        }
        if (d.contains("COMPRA") || d.contains("EMISSAO") || d.contains("AQUISICAO") || d.contains("SUBSCRICAO")) {
            return "COMPRA_ATIVO";
        }
        if (d.contains("RESGATE") || d.contains("VENDA") || d.contains("VENCIMENTO")) {
            return "VENDA_ATIVO";
        }
        if (d.contains("LIQ BOLSA")) {
            return "OPERACAO_BOLSA";
        }
        if (d.contains("TRANSFER")) {
            return "TRANSFERENCIA";
        }
        if (d.contains("IRRF") || d.contains("IOF") || d.contains("FEE") || d.contains("TARIFA") || d.contains("TAXA")) {
            return "IMPOSTO_TAXA";
        }
        return "OUTROS";
    }

    public List<AtivoDetectadoDTO> extrairAtivosDoExtrato(byte[] arquivoBytes, String senha) {
        Map<String, AtivoDetectadoDTO> map = new java.util.LinkedHashMap<>();
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            if (linhas == null) return new ArrayList<>();

            for (int i = 0; i < linhas.size(); i++) {
                String linha = limparTexto(String.join(" ", linhas.get(i)));

                // 1. Posição ETF
                Matcher mEtf = Pattern.compile("\\b([A-Z0-9]{4,6})\\s+(ETF\\s+[A-Z0-9\\s]+?)\\s+\\d+\\s+[\\d,]+", Pattern.CASE_INSENSITIVE).matcher(linha);
                if (mEtf.find()) {
                    String ticker = mEtf.group(1).toUpperCase().trim();
                    String nome = mEtf.group(2).trim();
                    map.put(ticker, AtivoDetectadoDTO.builder()
                            .ticker(ticker)
                            .nome(nome)
                            .tipo("ACAO")
                            .classe("ETF")
                            .indexador("Ibovespa")
                            .build());
                }

                // 2. Posição FII
                Matcher mFii = Pattern.compile("\\b([A-Z0-9]{4,6})\\s+(FII\\s+[A-Z0-9\\s]+?)\\s+\\d+\\s+[\\d,]+", Pattern.CASE_INSENSITIVE).matcher(linha);
                if (mFii.find()) {
                    String ticker = mFii.group(1).toUpperCase().trim();
                    String nome = mFii.group(2).trim();
                    map.put(ticker, AtivoDetectadoDTO.builder()
                            .ticker(ticker)
                            .nome(nome)
                            .tipo("FII")
                            .classe("Fundos Imobiliários")
                            .indexador("IPCA")
                            .build());
                }

                // 3. Proventos / Rendimentos no extrato
                Matcher mProv = Pattern.compile("(?:s/\\s*(FII|ETF)?\\s*([A-Z0-9\\s]+?)\\s*-\\s*)?([A-Z]{4}[0-9]{1,2}[B]?)\\b", Pattern.CASE_INSENSITIVE).matcher(linha);
                if (mProv.find()) {
                    String prefixTipo = mProv.group(1);
                    String nomeAtivo = mProv.group(2);
                    String ticker = mProv.group(3).toUpperCase().trim();
                    if (!map.containsKey(ticker) && !ticker.equalsIgnoreCase("TOTAL") && !ticker.equalsIgnoreCase("DATA") && !ticker.equalsIgnoreCase("SALDO")) {
                        String tipoAtivo = "ACAO";
                        String classeAtivo = "Ações";
                        String nomeFinal = (nomeAtivo != null && !nomeAtivo.trim().isEmpty()) ? nomeAtivo.trim() : ticker;

                        if ("FII".equalsIgnoreCase(prefixTipo) || (ticker.endsWith("11") && !ticker.startsWith("AURO"))) {
                            tipoAtivo = "FII";
                            classeAtivo = "Fundos Imobiliários";
                            if (!nomeFinal.toUpperCase().startsWith("FII")) {
                                nomeFinal = "FII " + nomeFinal;
                            }
                        } else if ("ETF".equalsIgnoreCase(prefixTipo) || ticker.startsWith("AURO") || ticker.startsWith("BOVA") || ticker.startsWith("IVVB") || ticker.startsWith("SMAL")) {
                            tipoAtivo = "ACAO";
                            classeAtivo = "ETF";
                            if (!nomeFinal.toUpperCase().startsWith("ETF")) {
                                nomeFinal = "ETF " + nomeFinal;
                            }
                        }

                        map.put(ticker, AtivoDetectadoDTO.builder()
                                .ticker(ticker)
                                .nome(nomeFinal)
                                .tipo(tipoAtivo)
                                .classe(classeAtivo)
                                .indexador(tipoAtivo.equals("FII") ? "IPCA" : "Ibovespa")
                                .build());
                    }
                }

                // 4. Posição CDB
                Matcher mCdb = Pattern.compile("\\b(CDB[0-9A-Z]{6,12})\\s+(\\d{2}/\\d{2}/\\d{2,4})\\s+(\\d{2}/\\d{2}/\\d{2,4})").matcher(linha);
                if (mCdb.find()) {
                    String ticker = mCdb.group(1).toUpperCase().trim();
                    String dtVenc = mCdb.group(3).trim();
                    if (!map.containsKey(ticker)) {
                        map.put(ticker, AtivoDetectadoDTO.builder()
                                .ticker(ticker)
                                .nome("CDB Banco BTG Pactual (Venc: " + dtVenc + ")")
                                .tipo("RENDA_FIXA")
                                .classe("CDB")
                                .indexador("CDI")
                                .build());
                    }
                }
            }
        } catch (Exception ignored) {}
        return new ArrayList<>(map.values());
    }

}
