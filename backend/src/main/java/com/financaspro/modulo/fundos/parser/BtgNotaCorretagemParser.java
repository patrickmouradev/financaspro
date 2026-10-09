package com.financaspro.modulo.fundos.parser;

import com.financaspro.utils.CsvExcelParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BtgNotaCorretagemParser {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Pattern PATTERN_NEGOCIO = Pattern.compile(
            "(?:\\d-BOVESPA|BOVESPA)?\\s*(?<tipo>[CV])\\s*(?:VISTA|OPCAO|FUTURO|TERMO)?\\s*(?<ticker>[A-Z]{4}[0-9]{1,2})\\s*(?<qtd>\\d+)\\s*(?<preco>\\d+[.,]\\d{2})\\s*(?<valor>\\d+[.,]\\d{2})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PATTERN_DATA = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b");
    private static final Pattern PATTERN_NOTA_NUM = Pattern.compile("\\b(\\d{6,10})\\b");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotaCorretagemDTO {
        private String numeroNota;
        private LocalDate dataPregao;
        private LocalDate dataLiquidacao;
        private BigDecimal totalOperacoes;
        private BigDecimal taxaLiquidacao;
        private BigDecimal emolumentos;
        private BigDecimal totalTaxas;
        private BigDecimal valorLiquidoNota;
        private List<ItemNotaDTO> itens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemNotaDTO {
        private String negoceacao;
        private String tipoOperacao; // C (Compra) ou V (Venda)
        private String tipoMercado;
        private String ticker;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal valorTotalOperacao;
        private BigDecimal taxasProrrateadas;
        private BigDecimal precoMedioAjustadoComTaxas;
    }

    public boolean ehFormatoNotaCorretagem(byte[] arquivoBytes, String nomeArquivo, String senha) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("NOTA") || nomeArquivo.toUpperCase().contains("CORRETAGEM"))) {
            return true;
        }
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            for (List<String> colunas : linhas) {
                for (String cel : colunas) {
                    String lower = cel.toLowerCase();
                    if (lower.contains("nota de corretagem") || lower.contains("resumo dos negócios") || lower.contains("resumo financeiro")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public NotaCorretagemDTO parsearNotaCorretagem(byte[] arquivoBytes, String senha) throws Exception {
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);

        String numeroNota = "";
        LocalDate dataPregao = null;
        LocalDate dataLiquidacao = null;

        BigDecimal totalOperacoes = BigDecimal.ZERO;
        BigDecimal taxaLiquidacao = BigDecimal.ZERO;
        BigDecimal emolumentos = BigDecimal.ZERO;
        BigDecimal taxasOutras = BigDecimal.ZERO;

        List<ItemNotaDTO> itensRaw = new ArrayList<>();

        for (List<String> linha : linhas) {
            if (linha == null || linha.isEmpty()) {
                continue;
            }

            String linhaCompleta = String.join(" ", linha).trim();
            String linhaUpper = linhaCompleta.toUpperCase();

            // 1. Extrai Número da Nota
            if (numeroNota.isEmpty() && (linhaUpper.contains("NR. NOTA") || linhaUpper.contains("NÚMERO NOTA") || linhaUpper.contains("NOTA DE CORRETAGEM"))) {
                Matcher mNota = PATTERN_NOTA_NUM.matcher(linhaUpper);
                if (mNota.find()) {
                    numeroNota = mNota.group(1);
                }
            }

            // 2. Extrai Data Pregão
            if (dataPregao == null && (linhaUpper.contains("DATA PREGÃO") || linhaUpper.contains("DATA PREGAO") || linhaUpper.contains("PREGÃO"))) {
                Matcher mData = PATTERN_DATA.matcher(linhaUpper);
                if (mData.find()) {
                    try {
                        dataPregao = LocalDate.parse(mData.group(1), FORMATO_DATA_BR);
                    } catch (Exception ignored) {}
                }
            }

            // 3. Extrai Data de Liquidação
            if (dataLiquidacao == null && (linhaUpper.contains("LÍQUIDO PARA") || linhaUpper.contains("LIQUIDO PARA"))) {
                Matcher mLiq = PATTERN_DATA.matcher(linhaUpper);
                if (mLiq.find()) {
                    try {
                        dataLiquidacao = LocalDate.parse(mLiq.group(1), FORMATO_DATA_BR);
                    } catch (Exception ignored) {}
                }
            }

            // 4. Extrai Taxas B3
            if (linhaUpper.contains("TAXA DE LIQUIDAÇÃO") || linhaUpper.contains("TAXA DE LIQUIDACAO")) {
                BigDecimal val = extrairUltimoValorMoeda(linhaUpper);
                if (val != null) taxaLiquidacao = val;
            } else if (linhaUpper.contains("EMOLUMENTOS")) {
                BigDecimal val = extrairUltimoValorMoeda(linhaUpper);
                if (val != null) emolumentos = val;
            } else if (linhaUpper.contains("TAXA DE TRANSFERENCIA") || linhaUpper.contains("TAXA DE REGISTRO") || linhaUpper.contains("DEPOSITÁRIA")) {
                BigDecimal val = extrairUltimoValorMoeda(linhaUpper);
                if (val != null) taxasOutras = taxasOutras.add(val);
            } else if (linhaUpper.contains("VALOR LÍQUIDO DAS OPERAÇÕES") || linhaUpper.contains("COMPRAS À VISTA")) {
                BigDecimal val = extrairUltimoValorMoeda(linhaUpper);
                if (val != null && val.compareTo(BigDecimal.ZERO) > 0) {
                    totalOperacoes = val;
                }
            }

            // 5. Linha de negócio
            Matcher mNegocio = PATTERN_NEGOCIO.matcher(linhaCompleta);
            if (mNegocio.find()) {
                String tipoOp = mNegocio.group("tipo").toUpperCase();
                String ticker = mNegocio.group("ticker").toUpperCase();
                BigDecimal qtd = new BigDecimal(mNegocio.group("qtd"));
                BigDecimal preco = parseBigDecimal(mNegocio.group("preco"));
                BigDecimal valorOp = parseBigDecimal(mNegocio.group("valor"));

                if (valorOp == null && qtd != null && preco != null) {
                    valorOp = qtd.multiply(preco);
                }

                if (ticker != null && qtd != null && preco != null && valorOp != null) {
                    itensRaw.add(ItemNotaDTO.builder()
                            .negoceacao("1-BOVESPA")
                            .tipoOperacao(tipoOp)
                            .tipoMercado("VISTA")
                            .ticker(ticker)
                            .quantidade(qtd)
                            .precoUnitario(preco)
                            .valorTotalOperacao(valorOp)
                            .taxasProrrateadas(BigDecimal.ZERO)
                            .precoMedioAjustadoComTaxas(preco)
                            .build());
                }
            }
        }

        if (totalOperacoes.compareTo(BigDecimal.ZERO) == 0 && !itensRaw.isEmpty()) {
            for (ItemNotaDTO item : itensRaw) {
                totalOperacoes = totalOperacoes.add(item.getValorTotalOperacao());
            }
        }

        BigDecimal totalTaxasNota = taxaLiquidacao.add(emolumentos).add(taxasOutras);
        List<ItemNotaDTO> itensProcessados = new ArrayList<>();

        for (ItemNotaDTO item : itensRaw) {
            BigDecimal taxaProporcional = BigDecimal.ZERO;
            if (totalOperacoes.compareTo(BigDecimal.ZERO) > 0 && totalTaxasNota.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal proporcao = item.getValorTotalOperacao().divide(totalOperacoes, 8, RoundingMode.HALF_UP);
                taxaProporcional = totalTaxasNota.multiply(proporcao).setScale(4, RoundingMode.HALF_UP);
            }

            BigDecimal custoTotalItem = "C".equalsIgnoreCase(item.getTipoOperacao())
                    ? item.getValorTotalOperacao().add(taxaProporcional)
                    : item.getValorTotalOperacao().subtract(taxaProporcional);

            BigDecimal precoMedioAjustado = custoTotalItem.divide(item.getQuantidade(), 4, RoundingMode.HALF_UP);

            item.setTaxasProrrateadas(taxaProporcional);
            item.setPrecoMedioAjustadoComTaxas(precoMedioAjustado);
            itensProcessados.add(item);
        }

        return NotaCorretagemDTO.builder()
                .numeroNota(numeroNota)
                .dataPregao(dataPregao != null ? dataPregao : LocalDate.now())
                .dataLiquidacao(dataLiquidacao)
                .totalOperacoes(totalOperacoes)
                .taxaLiquidacao(taxaLiquidacao)
                .emolumentos(emolumentos)
                .totalTaxas(totalTaxasNota)
                .valorLiquidoNota(totalOperacoes.add(totalTaxasNota))
                .itens(itensProcessados)
                .build();
    }

    private BigDecimal extrairUltimoValorMoeda(String texto) {
        if (texto == null) return null;
        Matcher m = Pattern.compile("(\\d{1,3}(?:\\.\\d{3})*,\\d{2})").matcher(texto);
        String ultimo = null;
        while (m.find()) {
            ultimo = m.group(1);
        }
        return parseBigDecimal(ultimo);
    }

    private BigDecimal parseBigDecimal(String valorStr) {
        if (valorStr == null || valorStr.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanVal = valorStr.replace("R$", "").replace("D", "").replace("C", "").replace(" ", "").trim();
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
}
