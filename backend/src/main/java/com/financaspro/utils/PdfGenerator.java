package com.financaspro.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;

public final class PdfGenerator {

    private PdfGenerator() {
        // Construtor privado para classe utilitária
    }

    public static byte[] gerarRelatorioSimples(String titulo, String conteudoTextual) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Paragraph pTitulo = new Paragraph(titulo)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(pTitulo);

        Paragraph pConteudo = new Paragraph(conteudoTextual)
                .setFontSize(12)
                .setMarginTop(20);
        document.add(pConteudo);

        document.close();
        return baos.toByteArray();
    }
}
