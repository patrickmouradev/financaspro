package com.financaspro.service;

import com.financaspro.utils.CsvExcelParser;
import com.financaspro.modulo.rendafixa.parser.BtgRendaFixaParser;
import com.financaspro.modulo.rendafixa.parser.BtgRendaFixaParser.RendaFixaNotaDTO;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

class PdfParserDebugTest {

    @Test
    void debugPdfParse() throws Exception {
        File file = new File("C:\\Users\\Patrick\\.gemini\\antigravity\\brain\\d76dea58-1172-4906-944f-3ba3a8e4e98f\\.user_uploaded\\media_1791156054811.pdf");
        if (!file.exists()) {
            System.out.println("Arquivo PDF não encontrado!");
            return;
        }

        byte[] bytes = Files.readAllBytes(file.toPath());
        List<List<String>> linhas = CsvExcelParser.obterLinhasDoArquivo(bytes, "");

        System.out.println("=== LINHAS EXTRAÍDAS DO PDF VIA iTEXT ===");
        for (int i = 0; i < linhas.size(); i++) {
            System.out.println("Linha " + i + ": " + linhas.get(i));
        }

        BtgRendaFixaParser parser = new BtgRendaFixaParser();
        RendaFixaNotaDTO dto = parser.parsearComprovanteRendaFixa(bytes, "");

        System.out.println("\n=== DTO RESULTANTE ===");
        System.out.println("Produto: " + dto.getProduto());
        System.out.println("Índice: " + dto.getIndice());
        System.out.println("Taxa: " + dto.getTaxa());
        System.out.println("Emissor: " + dto.getEmissor());
        System.out.println("CNPJ Emissor: " + dto.getCnpjEmissor());
        System.out.println("Data Aplicação: " + dto.getDataAplicacao());
        System.out.println("Data Vencimento: " + dto.getDataVencimento());
        System.out.println("Liquidez: " + dto.getLiquidez());
        System.out.println("Valor Aplicação: " + dto.getValorAplicacao());
    }
}
