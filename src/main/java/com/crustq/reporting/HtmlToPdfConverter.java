package com.crustq.reporting;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts print-friendly HTML into PDF for module-wise and scenario reports.
 */
public final class HtmlToPdfConverter {

    private HtmlToPdfConverter() {
    }

    public static void writePdf(String html, String pdfPath) {
        Path output = Path.of(pdfPath);
        try {
            Files.createDirectories(output.getParent());
            try (OutputStream outputStream = Files.newOutputStream(output)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, output.getParent().toUri().toString());
                builder.toStream(outputStream);
                builder.run();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write PDF report: " + pdfPath, e);
        }
    }
}
