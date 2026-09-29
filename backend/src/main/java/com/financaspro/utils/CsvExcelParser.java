package com.financaspro.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class CsvExcelParser {

    private CsvExcelParser() {
        // Construtor privado para classe utilitária
    }

    public static List<List<String>> parsearCsv(InputStream inputStream) throws Exception {
        List<List<String>> linhas = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String linhaTexto = "";
        while ((linhaTexto = reader.readLine()) != null) {
            if (linhaTexto.trim().isEmpty()) {
                continue;
            }
            String[] colunasArray = linhaTexto.split(";");
            if (colunasArray.length == 1) {
                colunasArray = linhaTexto.split(",");
            }
            List<String> colunasList = new ArrayList<>();
            for (String col : colunasArray) {
                colunasList.add(col.replaceAll("^\"|\"$", "").trim());
            }
            linhas.add(colunasList);
        }
        return linhas;
    }

    public static Workbook abrirWorkbookComSenha(byte[] arquivoBytes, String senha) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(arquivoBytes);
        if (senha != null && !senha.trim().isEmpty()) {
            return WorkbookFactory.create(is, senha.trim());
        } else {
            return WorkbookFactory.create(is);
        }
    }

    public static List<List<String>> parsearExcelWorkbook(Workbook workbook) {
        List<List<String>> linhas = new ArrayList<>();
        Sheet aba = workbook.getSheetAt(0);
        for (Row linha : aba) {
            List<String> colunas = new ArrayList<>();
            boolean linhaVazia = true;
            for (int c = 0; c < linha.getLastCellNum(); c++) {
                Cell celula = linha.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String valor = obterValorTextoCelula(celula);
                if (!valor.trim().isEmpty()) {
                    linhaVazia = false;
                }
                colunas.add(valor);
            }
            if (!linhaVazia) {
                linhas.add(colunas);
            }
        }
        return linhas;
    }

    public static String obterValorTextoCelula(Cell celula) {
        if (celula == null) {
            return "";
        }
        switch (celula.getCellType()) {
            case STRING:
                return celula.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(celula)) {
                    Date data = celula.getDateCellValue();
                    LocalDateTime ldt = data.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    return DateUtils.formatarDataHoraBr(ldt);
                }
                return String.valueOf(celula.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(celula.getBooleanCellValue());
            case FORMULA:
                try {
                    return celula.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(celula.getNumericCellValue());
                }
            default:
                return "";
        }
    }
}
