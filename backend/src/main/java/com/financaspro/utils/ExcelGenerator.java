package com.financaspro.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

public final class ExcelGenerator {

    private ExcelGenerator() {
        // Construtor privado para classe utilitária
    }

    public static byte[] gerarPlanilha(String nomeAba, List<String> cabecalho, List<List<String>> dados) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet aba = workbook.createSheet(nomeAba);

        int numLinha = 0;
        if (cabecalho != null && !cabecalho.isEmpty()) {
            Row linhaCabecalho = aba.createRow(numLinha++);
            for (int c = 0; c < cabecalho.size(); c++) {
                Cell celula = linhaCabecalho.createCell(c);
                celula.setCellValue(cabecalho.get(c));
            }
        }

        if (dados != null) {
            for (List<String> linhaDados : dados) {
                Row linha = aba.createRow(numLinha++);
                for (int c = 0; c < linhaDados.size(); c++) {
                    Cell celula = linha.createCell(c);
                    celula.setCellValue(linhaDados.get(c));
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        return baos.toByteArray();
    }
}
