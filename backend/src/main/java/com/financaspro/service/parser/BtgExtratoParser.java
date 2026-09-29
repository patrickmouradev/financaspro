package com.financaspro.service.parser;

import com.financaspro.model.entity.Lancamento;
import com.financaspro.utils.CsvExcelParser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class BtgExtratoParser {

    private static final DateTimeFormatter FORMATO_DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public boolean ehFormatoBtgExtrato(byte[] arquivoBytes, String nomeArquivo) {
        if (nomeArquivo != null && (nomeArquivo.toUpperCase().contains("EXTRATO") || nomeArquivo.endsWith(".xls"))) {
            return true;
        }
        try {
            Workbook wb = CsvExcelParser.abrirWorkbookComSenha(arquivoBytes, "");
            Sheet sheet = wb.getSheetAt(0);
            Row row1 = sheet.getRow(0);
            if (row1 != null) {
                Cell cell = row1.getCell(0);
                if (cell != null && cell.getStringCellValue().toLowerCase().contains("extrato de conta corrente")) {
                    wb.close();
                    return true;
                }
            }
            wb.close();
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<Lancamento> parsearExtrato(byte[] arquivoBytes) throws Exception {
        List<Lancamento> lancamentos = new ArrayList<>();
        Workbook workbook = CsvExcelParser.abrirWorkbookComSenha(arquivoBytes, "");
        Sheet sheet = workbook.getSheetAt(0);

        boolean dentroDaTabelaExtrato = false;

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String colA = CsvExcelParser.obterValorTextoCelula(row.getCell(0));
            String colB = CsvExcelParser.obterValorTextoCelula(row.getCell(1));

            // Detecta a linha de cabeçalho dos lançamentos (Data e hora | Categoria | Transação | Descrição | Valor)
            if ("Data e hora".equalsIgnoreCase(colA) && "Categoria".equalsIgnoreCase(colB)) {
                dentroDaTabelaExtrato = true;
                continue;
            }

            if (!dentroDaTabelaExtrato) {
                continue;
            }

            // Ignora linhas de Saldo Diário, Ouvidoria ou Rodapé
            if ("Saldo Diário".equalsIgnoreCase(colB) || colA.toLowerCase().contains("ouvidoria") || colA.contains("©")) {
                if (colA.toLowerCase().contains("ouvidoria")) {
                    dentroDaTabelaExtrato = false; // Fim do bloco de extrato
                }
                continue;
            }

            LocalDateTime dataHora = extrairDataHora(row.getCell(0), colA);
            if (dataHora == null) {
                continue;
            }

            String categoriaBtgOriginal = CsvExcelParser.obterValorTextoCelula(row.getCell(1));
            String transacao = CsvExcelParser.obterValorTextoCelula(row.getCell(2));
            String descricao = CsvExcelParser.obterValorTextoCelula(row.getCell(3));
            String valorText = CsvExcelParser.obterValorTextoCelula(row.getCell(4));

            if (descricao.isEmpty() || valorText.isEmpty()) {
                continue;
            }

            BigDecimal valor = new BigDecimal(valorText.replace(",", "."));

            String tipoLancamento = mapearTipoTransacao(transacao);

            Lancamento lancamento = Lancamento.builder()
                    .dataLancamento(dataHora)
                    .descricao(descricao)
                    .valor(valor)
                    .tipo(tipoLancamento)
                    .origem("BTG_EXTRATO")
                    .observacao("Categoria BTG: " + categoriaBtgOriginal + " | Transação: " + transacao)
                    .statusCategorizacao("PENDENTE")
                    .build();

            lancamentos.add(lancamento);
        }

        workbook.close();
        return lancamentos;
    }

    private LocalDateTime extrairDataHora(Cell celula, String textoData) {
        if (celula != null && celula.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(celula)) {
            Date data = celula.getDateCellValue();
            return data.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        if (textoData != null && !textoData.trim().isEmpty()) {
            try {
                return LocalDateTime.parse(textoData.trim(), FORMATO_DATA_HORA_BR);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
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
