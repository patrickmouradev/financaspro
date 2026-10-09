package com.financaspro.modulo.containvestimento.parser;

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
import java.util.List;

@Component
public class BtgExtratoInvestimentoParser {

    private static final DateTimeFormatter FORMATO_DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

    public boolean ehFormatoBtgInvestimento(byte[] arquivoBytes, String nomeArquivo, String senha) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("INVEST") || nomeArquivo.toUpperCase().contains("AUVP"))) {
            return true;
        }
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            for (List<String> colunas : linhas) {
                for (String cel : colunas) {
                    String lower = cel.toLowerCase();
                    if (lower.contains("extrato de conta investimento") || lower.contains("auvp capital")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<ItemExtratoInvestimento> parsearExtratoInvestimento(byte[] arquivoBytes, String senha) throws Exception {
        List<ItemExtratoInvestimento> itens = new ArrayList<>();
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);

        int colData = -1;
        int colDesc = -1;
        int colDebito = -1;
        int colCredito = -1;
        int colSaldo = -1;

        for (List<String> linha : linhas) {
            if (linha == null || linha.isEmpty()) {
                continue;
            }

            if (colData == -1 || (colDebito == -1 && colCredito == -1)) {
                int cData = -1, cDesc = -1, cDebito = -1, cCredito = -1, cSaldo = -1;
                for (int c = 0; c < linha.size(); c++) {
                    String cel = linha.get(c).trim().toLowerCase();
                    if (cel.contains("data")) cData = c;
                    else if (cel.contains("descri")) cDesc = c;
                    else if (cel.contains("déb") || cel.contains("debito")) cDebito = c;
                    else if (cel.contains("cré") || cel.contains("credito")) cCredito = c;
                    else if (cel.contains("saldo")) cSaldo = c;
                }
                if (cData != -1 && (cDebito != -1 || cCredito != -1)) {
                    colData = cData;
                    colDesc = cDesc;
                    colDebito = cDebito;
                    colCredito = cCredito;
                    colSaldo = cSaldo;
                    continue;
                }
            }

            if (colData == -1 || (colDebito == -1 && colCredito == -1)) {
                continue;
            }

            String dataStr = (colData < linha.size()) ? linha.get(colData).trim() : "";
            if (dataStr.isEmpty()) {
                continue;
            }

            String dataLower = dataStr.toLowerCase();
            if (dataLower.contains("data") || dataLower.contains("extrato") || dataLower.contains("cliente")
                    || dataLower.contains("cpf") || dataLower.contains("saldo inicial") || dataLower.contains("saldo final")
                    || dataLower.contains("total de")) {
                continue;
            }

            LocalDateTime dataHora = extrairDataHora(dataStr);
            if (dataHora == null) {
                continue;
            }

            String descricaoStr = (colDesc != -1 && colDesc < linha.size()) ? linha.get(colDesc).trim() : "";
            if (descricaoStr.isEmpty() || descricaoStr.equalsIgnoreCase("Saldo Inicial") || descricaoStr.equalsIgnoreCase("Saldo Final")) {
                continue;
            }

            String debitoStr = (colDebito != -1 && colDebito < linha.size()) ? linha.get(colDebito) : "";
            String creditoStr = (colCredito != -1 && colCredito < linha.size()) ? linha.get(colCredito) : "";
            String saldoStr = (colSaldo != -1 && colSaldo < linha.size()) ? linha.get(colSaldo) : "";

            BigDecimal debitoVal = parseBigDecimal(debitoStr);
            BigDecimal creditoVal = parseBigDecimal(creditoStr);
            BigDecimal saldoVal = parseBigDecimal(saldoStr);

            BigDecimal valorFinal = null;
            if (creditoVal != null && creditoVal.compareTo(BigDecimal.ZERO) > 0) {
                valorFinal = creditoVal;
            } else if (debitoVal != null && debitoVal.compareTo(BigDecimal.ZERO) > 0) {
                valorFinal = debitoVal.negate();
            } else if (debitoVal != null && debitoVal.compareTo(BigDecimal.ZERO) < 0) {
                valorFinal = debitoVal;
            }

            if (valorFinal == null) {
                continue;
            }

            String tickerExtraido = extrairTicker(descricaoStr);
            String tipoLancamento = mapearTipoInvestimento(descricaoStr);

            itens.add(ItemExtratoInvestimento.builder()
                    .dataMovimento(dataHora)
                    .descricao(descricaoStr)
                    .tipo(tipoLancamento)
                    .ticker(tickerExtraido)
                    .valor(valorFinal)
                    .saldoResultante(saldoVal)
                    .origem("BTG_INVESTIMENTO")
                    .observacao("Extrato Conta Investimento AUVP / BTG Pactual")
                    .build());
        }

        return itens;
    }

    private BigDecimal parseBigDecimal(String valorStr) {
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

    private LocalDateTime extrairDataHora(String textoData) {
        if (textoData == null || textoData.trim().isEmpty()) {
            return null;
        }
        String t = textoData.trim();
        try {
            return LocalDateTime.parse(t, FORMATO_DATA_HORA_BR);
        } catch (Exception e1) {
            try {
                LocalDate ld = LocalDate.parse(t, FORMATO_DATA_BR);
                return ld.atStartOfDay();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String extrairTicker(String desc) {
        if (desc == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\b[A-Z]{4}[0-9]{1,2}\\b");
        java.util.regex.Matcher m = p.matcher(desc.toUpperCase());
        if (m.find()) {
            return m.group();
        }
        return null;
    }

    private String mapearTipoInvestimento(String desc) {
        if (desc == null) return "OUTROS";
        String d = desc.toUpperCase();

        if (d.contains("RENDIMENTOS") || d.contains("DIVIDENDOS") || d.contains("PROVENTOS") || d.contains("JCP")) {
            return "DIVIDENDO";
        }
        if (d.contains("CUPOM") || d.contains("CONTA REMUNERADA") || d.contains("JUROS")) {
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
        if (d.contains("TRANSFERÊNCIA") || d.contains("TRANSFERENCIA") || d.contains("TED") || d.contains("PIX")) {
            return "TRANSFERENCIA";
        }
        if (d.contains("IRRF") || d.contains("IOF") || d.contains("FEE")) {
            return "IMPOSTO_TAXA";
        }
        return "OUTROS";
    }
}
