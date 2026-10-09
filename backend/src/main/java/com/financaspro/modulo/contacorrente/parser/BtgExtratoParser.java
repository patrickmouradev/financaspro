package com.financaspro.modulo.contacorrente.parser;

import com.financaspro.model.entity.Lancamento;
import com.financaspro.utils.CsvExcelParser;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class BtgExtratoParser {

    private static final DateTimeFormatter FORMATO_DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public boolean ehFormatoBtgExtrato(byte[] arquivoBytes, String nomeArquivo, String senha) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("EXTRATO") || nomeArquivo.toLowerCase().endsWith(".xls") || nomeArquivo.toLowerCase().endsWith(".csv"))) {
            return true;
        }
        try {
            List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);
            for (List<String> colunas : linhas) {
                for (String cel : colunas) {
                    if (cel.toLowerCase().contains("extrato de conta corrente") || cel.toLowerCase().contains("extrato")) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<Lancamento> parsearExtrato(byte[] arquivoBytes, String senha) throws Exception {
        List<Lancamento> lancamentos = new ArrayList<>();
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(arquivoBytes, senha);

        int colData = -1;
        int colCat = -1;
        int colTrans = -1;
        int colDesc = -1;
        int colValor = -1;

        for (List<String> linha : linhas) {
            if (linha == null || linha.isEmpty()) {
                continue;
            }

            if (colData == -1 || colValor == -1) {
                int cData = -1, cCat = -1, cTrans = -1, cDesc = -1, cValor = -1;
                for (int c = 0; c < linha.size(); c++) {
                    String cel = linha.get(c).trim().toLowerCase();
                    if (cel.contains("data")) cData = c;
                    else if (cel.contains("categoria")) cCat = c;
                    else if (cel.contains("transa")) cTrans = c;
                    else if (cel.contains("descri")) cDesc = c;
                    else if (cel.contains("valor")) cValor = c;
                }
                if (cData != -1 && cValor != -1) {
                    colData = cData;
                    colCat = cCat;
                    colTrans = cTrans;
                    colDesc = cDesc;
                    colValor = cValor;
                    continue;
                }
            }

            if (colData == -1 || colValor == -1) {
                continue;
            }

            String dataStr = (colData < linha.size()) ? linha.get(colData).trim() : "";
            if (dataStr.isEmpty()) {
                continue;
            }

            String dataLower = dataStr.toLowerCase();
            if (dataLower.contains("data") || dataLower.contains("extrato") || dataLower.contains("cliente") || dataLower.contains("cpf") || dataLower.contains("ouvidoria") || dataLower.contains("©")) {
                continue;
            }

            LocalDateTime dataHora = extrairDataHora(dataStr);
            if (dataHora == null) {
                continue;
            }

            String categoriaBtgStr = (colCat != -1 && colCat < linha.size()) ? linha.get(colCat).trim() : "";
            String transacaoStr = (colTrans != -1 && colTrans < linha.size()) ? linha.get(colTrans).trim() : "";
            String descricaoStr = (colDesc != -1 && colDesc < linha.size()) ? linha.get(colDesc).trim() : "";
            String valorStr = (colValor < linha.size()) ? linha.get(colValor).trim() : "";

            if (descricaoStr.equalsIgnoreCase("Saldo Diário") || transacaoStr.equalsIgnoreCase("Saldo Diário") || categoriaBtgStr.equalsIgnoreCase("Saldo Diário")) {
                continue;
            }

            if (descricaoStr.isEmpty() && !transacaoStr.isEmpty()) {
                descricaoStr = transacaoStr;
            }

            if (descricaoStr.isEmpty() || valorStr.isEmpty()) {
                continue;
            }

            BigDecimal valor;
            try {
                String cleanVal = valorStr.replace("R$", "").replace(" ", "").trim();
                if (cleanVal.contains(",") && cleanVal.contains(".")) {
                    cleanVal = cleanVal.replace(".", "").replace(",", ".");
                } else if (cleanVal.contains(",")) {
                    cleanVal = cleanVal.replace(",", ".");
                }
                valor = new BigDecimal(cleanVal);
            } catch (Exception e) {
                continue;
            }

            String tipoLancamento = mapearTipoTransacao(transacaoStr);

            Lancamento lancamento = Lancamento.builder()
                    .dataLancamento(dataHora)
                    .descricao(descricaoStr)
                    .valor(valor)
                    .tipo(tipoLancamento)
                    .origem("BTG_EXTRATO")
                    .observacao("Categoria BTG: " + categoriaBtgStr + " | Transação: " + transacaoStr)
                    .statusCategorizacao("PENDENTE")
                    .build();

            lancamentos.add(lancamento);
        }

        return lancamentos;
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
                try {
                    if (t.length() >= 10) {
                        LocalDate ld = LocalDate.parse(t.substring(0, 10));
                        return ld.atStartOfDay();
                    }
                } catch (Exception e3) {
                    return null;
                }
                return null;
            }
        }
    }

    private String mapearTipoTransacao(String transacao) {
        if (transacao == null) {
            return "OUTROS";
        }
        String t = transacao.toUpperCase();
        if (t.contains("PIX ENVIADO")) return "PIX_ENVIADO";
        if (t.contains("PIX RECEBIDO")) return "PIX_RECEBIDO";
        if (t.contains("DÉBITO") || t.contains("DEBITO")) return "DEBITO_CONTA";
        if (t.contains("BOLETO")) return "BOLETO";
        if (t.contains("FATURA")) return "PAGAMENTO_FATURA";
        if (t.contains("TRANSFERÊNCIA") || t.contains("TRANSFERENCIA")) return "TRANSFERENCIA";
        if (t.contains("JUROS")) return "JUROS";
        if (t.contains("IOF")) return "IOF";
        return "OUTROS";
    }
}
