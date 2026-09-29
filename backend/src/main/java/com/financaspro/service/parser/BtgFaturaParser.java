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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
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
        try {
            Workbook wb = CsvExcelParser.abrirWorkbookComSenha(arquivoBytes, "");
            Sheet sheet = wb.getSheetAt(0);
            Row row3 = sheet.getRow(2);
            if (row3 != null) {
                Cell cell = row3.getCell(1);
                if (cell != null && cell.getStringCellValue().toLowerCase().contains("fatura")) {
                    wb.close();
                    return true;
                }
            }
            wb.close();
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<Lancamento> parsearFatura(byte[] arquivoBytes, String senha) throws Exception {
        List<Lancamento> lancamentos = new ArrayList<>();
        Workbook workbook = CsvExcelParser.abrirWorkbookComSenha(arquivoBytes, senha);
        Sheet sheet = workbook.getSheetAt(0);

        boolean dentroDaTabelaCompras = false;

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            String colB = CsvExcelParser.obterValorTextoCelula(row.getCell(1));
            String colH = CsvExcelParser.obterValorTextoCelula(row.getCell(7));

            // Detecta a linha de cabeçalho das compras (Linha 25 na fatura real)
            if ("Data".equalsIgnoreCase(colB) && "Final Cartão".equalsIgnoreCase(colH)) {
                dentroDaTabelaCompras = true;
                continue;
            }

            if (!dentroDaTabelaCompras) {
                continue;
            }

            // Se for linha de resumo final ou linha vazia após a tabela, encerra
            if (colB.toLowerCase().contains("total") || colB.isEmpty()) {
                if (colB.toLowerCase().contains("total")) {
                    break;
                }
                continue;
            }

            LocalDateTime dataLancamento = extrairData(row.getCell(1), colB);
            if (dataLancamento == null) {
                continue;
            }

            String descricaoRaw = CsvExcelParser.obterValorTextoCelula(row.getCell(2));
            String valorText = CsvExcelParser.obterValorTextoCelula(row.getCell(4));
            String tipoCompra = CsvExcelParser.obterValorTextoCelula(row.getCell(5));
            String codigoAutorizacao = CsvExcelParser.obterValorTextoCelula(row.getCell(6));
            String finalCartao = CsvExcelParser.obterValorTextoCelula(row.getCell(7));

            if (descricaoRaw.isEmpty() || valorText.isEmpty()) {
                continue;
            }

            BigDecimal valor = new BigDecimal(valorText.replace(",", "."));
            if (valor.compareTo(BigDecimal.ZERO) < 0) {
                valor = valor.abs(); // Lançamento de compra é valor positivo no sistema
            }

            // Trata parcelas da descrição: "Otica Monica De São Be (8/10)"
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

        workbook.close();
        return lancamentos;
    }

    private LocalDateTime extrairData(Cell celula, String textoData) {
        if (celula != null && celula.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && DateUtil.isCellDateFormatted(celula)) {
            Date data = celula.getDateCellValue();
            return data.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        }
        if (textoData != null && !textoData.trim().isEmpty()) {
            try {
                return LocalDateTime.parse(textoData.trim(), FORMATO_DATA_HORA);
            } catch (Exception e1) {
                try {
                    LocalDate ld = LocalDate.parse(textoData.trim(), FORMATO_DATA_BR);
                    return ld.atStartOfDay();
                } catch (Exception e2) {
                    return null;
                }
            }
        }
        return null;
    }
}
