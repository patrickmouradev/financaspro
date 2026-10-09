package com.financaspro.modulo.faturacartao.parser;

import com.financaspro.model.entity.Lancamento;
import com.financaspro.utils.CsvExcelParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BtgFaturaParser {

    private static final Pattern PATTERN_PARCELA = Pattern.compile("\\((\\d+)/(\\d+)\\)$");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public boolean ehFormatoBtgFatura(byte[] arquivoBytes, String nomeArquivo) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("BTG") || nomeArquivo.toUpperCase().contains("FATURA"))) {
            return true;
        }
        return false;
    }

    public List<Lancamento> parsearFatura(byte[] arquivoBytes, String senha) throws Exception {
        List<Lancamento> lancamentos = new ArrayList<>();
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);

        int colData = -1;
        int colDesc = -1;
        int colValor = -1;
        int colTipo = -1;
        int colCodAut = -1;
        int colFinalCartao = -1;

        boolean dentroDaTabelaCompras = false;

        for (List<String> linha : linhas) {
            if (linha == null || linha.isEmpty()) {
                continue;
            }

            if (!dentroDaTabelaCompras) {
                int cData = -1, cDesc = -1, cValor = -1, cTipo = -1, cCod = -1, cFinal = -1;
                for (int c = 0; c < linha.size(); c++) {
                    String cel = linha.get(c).trim().toLowerCase();
                    if (cel.equals("data")) cData = c;
                    else if (cel.contains("estabelec") || cel.contains("descri")) cDesc = c;
                    else if (cel.contains("valor")) cValor = c;
                    else if (cel.contains("tipo")) cTipo = c;
                    else if (cel.contains("autori") || cel.contains("cód")) cCod = c;
                    else if (cel.contains("final cart") || cel.contains("cartão")) cFinal = c;
                }
                if (cData != -1 && cFinal != -1) {
                    colData = cData;
                    colDesc = cDesc;
                    colValor = cValor;
                    colTipo = cTipo;
                    colCodAut = cCod;
                    colFinalCartao = cFinal;
                    dentroDaTabelaCompras = true;
                    continue;
                }
            }

            if (!dentroDaTabelaCompras) {
                continue;
            }

            String dataStr = (colData != -1 && colData < linha.size()) ? linha.get(colData).trim() : "";
            if (dataStr.toLowerCase().contains("total") || dataStr.isEmpty()) {
                if (dataStr.toLowerCase().contains("total")) {
                    break;
                }
                continue;
            }

            LocalDateTime dataLancamento = extrairData(dataStr);
            if (dataLancamento == null) {
                continue;
            }

            String descricaoRaw = (colDesc != -1 && colDesc < linha.size()) ? linha.get(colDesc).trim() : "";
            String valorText = (colValor != -1 && colValor < linha.size()) ? linha.get(colValor).trim() : "";
            String tipoCompra = (colTipo != -1 && colTipo < linha.size()) ? linha.get(colTipo).trim() : "";
            String codigoAutorizacao = (colCodAut != -1 && colCodAut < linha.size()) ? linha.get(colCodAut).trim() : "";
            String finalCartao = (colFinalCartao != -1 && colFinalCartao < linha.size()) ? linha.get(colFinalCartao).trim() : "";

            if (descricaoRaw.isEmpty() || valorText.isEmpty()) {
                continue;
            }

            BigDecimal valor;
            try {
                String cleanVal = valorText.replace("R$", "").replace(" ", "").trim();
                if (cleanVal.contains(",") && cleanVal.contains(".")) {
                    cleanVal = cleanVal.replace(".", "").replace(",", ".");
                } else if (cleanVal.contains(",")) {
                    cleanVal = cleanVal.replace(",", ".");
                }
                valor = new BigDecimal(cleanVal);
                if (valor.compareTo(BigDecimal.ZERO) < 0) {
                    valor = valor.abs();
                }
            } catch (Exception e) {
                continue;
            }

            Integer parcelaAtual = null;
            Integer totalParcelas = null;
            String descricaoLimpa = descricaoRaw.trim();

            Matcher matcher = PATTERN_PARCELA.matcher(descricaoLimpa);
            if (matcher.find()) {
                parcelaAtual = Integer.parseInt(matcher.group(1));
                totalParcelas = Integer.parseInt(matcher.group(2));
                descricaoLimpa = matcher.replaceAll("").trim();
            }

            String tipoLancamento = tipoCompra.toLowerCase().contains("parcela") ? "PARCELADO" : "COMPRA_VISTA";

            Lancamento lancamento = Lancamento.builder()
                    .dataLancamento(dataLancamento)
                    .descricao(descricaoLimpa)
                    .valor(valor)
                    .tipo(tipoLancamento)
                    .origem("BTG_FATURA")
                    .codigoAutorizacao(codigoAutorizacao)
                    .parcelaAtual(parcelaAtual)
                    .totalParcelas(totalParcelas)
                    .observacao("Final Cartão: " + finalCartao)
                    .statusCategorizacao("PENDENTE")
                    .build();

            lancamentos.add(lancamento);
        }

        return lancamentos;
    }

    private LocalDateTime extrairData(String textoData) {
        if (textoData == null || textoData.trim().isEmpty()) {
            return null;
        }
        String t = textoData.trim();
        try {
            return LocalDateTime.parse(t, FORMATO_DATA_HORA);
        } catch (Exception e1) {
            try {
                LocalDate ld = LocalDate.parse(t, FORMATO_DATA_BR);
                return ld.atStartOfDay();
            } catch (Exception e2) {
                return null;
            }
        }
    }
}
