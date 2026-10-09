package com.financaspro.modulo.rendafixa.parser;

import com.financaspro.utils.CsvExcelParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BtgRendaFixaParser {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern PATTERN_DATA = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");
    private static final Pattern PATTERN_CNPJ = Pattern.compile("\\b(\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})\\b");
    private static final Pattern PATTERN_VALOR = Pattern.compile("(?:R\\$\\s*)?(\\d{1,3}(?:\\.\\d{3})*,\\d{2})");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RendaFixaNotaDTO {
        private String produto;             // CDB Pós-Fixado, Tesouro IPCA+, LCI, LCA (ou null)
        private String indice;              // CDI, IPCA, SELIC, PREFIXADO, IGP-M (ou null)
        private String taxa;                // 100.00% do CDI, 3.5%, IPCA + 6.5% (ou null)
        private String emissor;             // Banco BTG Pactual (ou null)
        private String cnpjEmissor;         // 30.306.294/0001-45 (ou null)
        private LocalDate dataAplicacao;    // 18/08/2026
        private LocalDate dataVencimento;   // 21/08/2028
        private String liquidez;            // Diária, No Vencimento (ou null)
        private BigDecimal valorAplicacao;  // R$ 299,96
        private String contaDv;
        private String titular;
        private String autenticacaoEletronica;
    }

    public boolean ehFormatoRendaFixa(byte[] arquivoBytes, String nomeArquivo, String senha) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("RENDA_FIXA")
                || nomeArquivo.toUpperCase().contains("RENDA FIXA")
                || nomeArquivo.toUpperCase().contains("COMPROVANTE")
                || nomeArquivo.toUpperCase().contains("APLICACAO"))) {
            return true;
        }
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            for (List<String> colunas : linhas) {
                String linhaStr = String.join(" ", colunas).toLowerCase();
                if (linhaStr.contains("comprovante de aplicação em renda fixa")
                        || linhaStr.contains("nota de negociação em renda fixa")
                        || linhaStr.contains("aplicação em renda fixa")
                        || linhaStr.contains("cdb pós-fixado")
                        || linhaStr.contains("data da aplicação")) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public RendaFixaNotaDTO parsearComprovanteRendaFixa(byte[] arquivoBytes, String senha) throws Exception {
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
        if (linhas == null || linhas.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível ler as linhas do documento de Renda Fixa.");
        }

        String produto = null;
        String indiceStr = null;
        String taxaStr = null;
        String emissor = null;
        String cnpjEmissor = null;
        LocalDate dataAplicacao = null;
        LocalDate dataVencimento = null;
        String liquidez = null;
        BigDecimal valorAplicacao = null;
        String contaDv = null;
        String titular = null;
        String autenticacaoEletronica = null;

        List<String> linhasTexto = new ArrayList<>();
        for (List<String> colunas : linhas) {
            if (colunas != null && !colunas.isEmpty()) {
                linhasTexto.add(String.join(" ", colunas).trim());
            }
        }

        for (int i = 0; i < linhasTexto.size(); i++) {
            String linha = linhasTexto.get(i);
            String linhaUpper = linha.toUpperCase();

            // Conta / DV
            if (contaDv == null && (linhaUpper.contains("CONTA/DV") || linhaUpper.contains("CONTA DV"))) {
                if (i + 1 < linhasTexto.size()) {
                    String prox = linhasTexto.get(i + 1);
                    Matcher m = Pattern.compile("\\b(\\d{5,10})\\b").matcher(prox);
                    if (m.find()) contaDv = m.group(1);
                }
            }

            // Titular
            if (titular == null && linhaUpper.contains("TITULAR")) {
                if (i + 1 < linhasTexto.size()) {
                    String prox = linhasTexto.get(i + 1).trim();
                    if (!prox.toUpperCase().contains("PRODUTO") && !prox.toUpperCase().contains("CONTA")) {
                        titular = prox;
                    }
                }
            }

            // Produto / Modalidade
            if (produto == null && (linhaUpper.contains("PRODUTO") || linhaUpper.contains("TÍTULO") || linhaUpper.contains("TITULO"))) {
                String val = extrairValorCampo(linha, "PRODUTO", "TÍTULO", "TITULO");
                if (val.isEmpty() && i + 1 < linhasTexto.size()) {
                    val = linhasTexto.get(i + 1).trim();
                }
                if (!val.isEmpty() && !val.toUpperCase().contains("ÍNDICE") && !val.toUpperCase().contains("INDICE")) {
                    produto = val;
                }
            }

            // Índice / Taxa / Porcentagem
            if ((indiceStr == null || taxaStr == null) && (linhaUpper.contains("ÍNDICE") || linhaUpper.contains("INDICE") || linhaUpper.contains("TAXA") || linhaUpper.contains("RENTABILIDADE"))) {
                String val = extrairValorCampo(linha, "ÍNDICE", "INDICE", "TAXA", "RENTABILIDADE");
                if (val.isEmpty() && i + 1 < linhasTexto.size()) {
                    val = linhasTexto.get(i + 1).trim();
                }
                if (!val.isEmpty()) {
                    taxaStr = val; // Ex: "100.00% do CDI" ou "IPCA + 6,5%" ou "12,5% a.a."
                }
            }

            // Emissor
            if (emissor == null && linhaUpper.contains("EMISSOR") && !linhaUpper.contains("CNPJ")) {
                String val = extrairValorCampo(linha, "EMISSOR");
                if (val.isEmpty() && i + 1 < linhasTexto.size()) {
                    val = linhasTexto.get(i + 1).trim();
                }
                if (!val.isEmpty() && !val.toUpperCase().contains("CNPJ") && !val.toUpperCase().contains("DATA")) {
                    emissor = val;
                }
            }

            // CNPJ Emissor
            if (cnpjEmissor == null && linhaUpper.contains("CNPJ")) {
                Matcher mCnpj = PATTERN_CNPJ.matcher(linhaUpper);
                if (mCnpj.find()) {
                    cnpjEmissor = mCnpj.group(1);
                } else if (i + 1 < linhasTexto.size()) {
                    Matcher mCnpj2 = PATTERN_CNPJ.matcher(linhasTexto.get(i + 1));
                    if (mCnpj2.find()) {
                        cnpjEmissor = mCnpj2.group(1);
                    }
                }
            }

            // Data da Aplicação / Compra
            if (dataAplicacao == null && (linhaUpper.contains("DATA DA APLICAÇÃO") || linhaUpper.contains("DATA DA APLICACAO") || linhaUpper.contains("DATA DA COMPRA") || linhaUpper.contains("DATA OPERAÇÃO"))) {
                Matcher mData = PATTERN_DATA.matcher(linhaUpper);
                if (mData.find()) {
                    dataAplicacao = parseDataBr(mData.group(1));
                } else if (i + 1 < linhasTexto.size()) {
                    Matcher mData2 = PATTERN_DATA.matcher(linhasTexto.get(i + 1));
                    if (mData2.find()) dataAplicacao = parseDataBr(mData2.group(1));
                }
            }

            // Data do Vencimento
            if (dataVencimento == null && (linhaUpper.contains("DATA DO VENCIMENTO") || linhaUpper.contains("VENCIMENTO"))) {
                Matcher mData = PATTERN_DATA.matcher(linhaUpper);
                if (mData.find()) {
                    dataVencimento = parseDataBr(mData.group(1));
                } else if (i + 1 < linhasTexto.size()) {
                    Matcher mData2 = PATTERN_DATA.matcher(linhasTexto.get(i + 1));
                    if (mData2.find()) dataVencimento = parseDataBr(mData2.group(1));
                }
            }

            // Liquidez
            if (liquidez == null && linhaUpper.contains("LIQUIDEZ")) {
                String val = extrairValorCampo(linha, "LIQUIDEZ");
                if (val.isEmpty() && i + 1 < linhasTexto.size()) {
                    val = linhasTexto.get(i + 1).trim();
                }
                if (!val.isEmpty() && !val.toUpperCase().contains("VALOR") && !val.toUpperCase().contains("AUTENTICAÇÃO")) {
                    liquidez = val;
                }
            }

            // Valor Aplicação
            if (valorAplicacao == null && (linhaUpper.contains("VALOR APLICAÇÃO") || linhaUpper.contains("VALOR APLICACAO") || linhaUpper.contains("VALOR DA OPERAÇÃO") || linhaUpper.contains("VALOR LÍQUIDO"))) {
                Matcher mVal = PATTERN_VALOR.matcher(linhaUpper);
                if (mVal.find()) {
                    valorAplicacao = parseBigDecimal(mVal.group(1));
                } else if (i + 1 < linhasTexto.size()) {
                    Matcher mVal2 = PATTERN_VALOR.matcher(linhasTexto.get(i + 1));
                    if (mVal2.find()) valorAplicacao = parseBigDecimal(mVal2.group(1));
                }
            }

            // Autenticação Eletrônica
            if (autenticacaoEletronica == null && (linhaUpper.contains("AUTENTICAÇÃO ELETRÔNICA") || linhaUpper.contains("AUTENTICACAO ELETRONICA"))) {
                Matcher mAuth = Pattern.compile("\\b([A-Z0-9]{25,50})\\b").matcher(linhaUpper);
                if (mAuth.find()) {
                    autenticacaoEletronica = mAuth.group(1);
                } else if (i + 1 < linhasTexto.size()) {
                    Matcher mAuth2 = Pattern.compile("\\b([A-Z0-9]{25,50})\\b").matcher(linhasTexto.get(i + 1).toUpperCase());
                    if (mAuth2.find()) autenticacaoEletronica = mAuth2.group(1);
                }
            }
        }

        // Validação estrita do Índice (Indexador): se não encontrado/identificado -> nulo
        if (taxaStr != null) {
            String upperTaxa = taxaStr.toUpperCase();
            if (upperTaxa.contains("CDI")) {
                indiceStr = "CDI";
            } else if (upperTaxa.contains("IPCA")) {
                indiceStr = "IPCA";
            } else if (upperTaxa.contains("SELIC")) {
                indiceStr = "SELIC";
            } else if (upperTaxa.contains("IGP-M") || upperTaxa.contains("IGPM")) {
                indiceStr = "IGP-M";
            } else if (upperTaxa.contains("PREFIXADO") || upperTaxa.contains("PRÉ-FIXADO") || upperTaxa.contains("% A.A.")) {
                indiceStr = "PREFIXADO";
            } else {
                indiceStr = null;
            }
        } else {
            indiceStr = null;
        }

        if (produto != null && produto.trim().length() < 2) {
            produto = null;
        }

        return RendaFixaNotaDTO.builder()
                .produto(produto)
                .indice(indiceStr)
                .taxa(taxaStr)
                .emissor(emissor)
                .cnpjEmissor(cnpjEmissor)
                .dataAplicacao(dataAplicacao != null ? dataAplicacao : LocalDate.now())
                .dataVencimento(dataVencimento)
                .liquidez(liquidez)
                .valorAplicacao(valorAplicacao != null ? valorAplicacao : BigDecimal.ZERO)
                .contaDv(contaDv)
                .titular(titular)
                .autenticacaoEletronica(autenticacaoEletronica)
                .build();
    }

    private String extrairValorCampo(String linha, String... chaves) {
        String upper = linha.toUpperCase();
        for (String chave : chaves) {
            int idx = upper.indexOf(chave.toUpperCase());
            if (idx != -1) {
                String sub = linha.substring(idx + chave.length()).trim();
                if (sub.startsWith(":") || sub.startsWith("-")) {
                    sub = sub.substring(1).trim();
                }
                return sub;
            }
        }
        return "";
    }

    private LocalDate parseDataBr(String strData) {
        try {
            return LocalDate.parse(strData.trim(), FORMATO_DATA_BR);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String strVal) {
        if (strVal == null) return null;
        try {
            String clean = strVal.replace("R$", "").replace(" ", "").trim();
            if (clean.contains(",") && clean.contains(".")) {
                clean = clean.replace(".", "").replace(",", ".");
            } else if (clean.contains(",")) {
                clean = clean.replace(",", ".");
            }
            return new BigDecimal(clean);
        } catch (Exception e) {
            return null;
        }
    }
}
