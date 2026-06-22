package com.crustq.reporting;

import com.crustq.config.ConfigReader;
import tech.grasshopper.reporter.ExtentPDFReporter;
import tech.grasshopper.reporter.config.ExtentPDFReporterConfig;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Configures the Grasshopper PDF reporter for ExtentReports v5.
 */
public final class PdfReportGenerator {

    private PdfReportGenerator() {
    }

    public static ExtentPDFReporter createReporter(String pdfReportPath) {
        ExtentPDFReporter pdfReporter = new ExtentPDFReporter(pdfReportPath);

        ExtentPDFReporterConfig config = ExtentPDFReporterConfig.builder()
                .title(ConfigReader.get("report.title", "Crust & Q Test Execution Report"))
                .displayAttributeSummary(true)
                .displayAttributeDetails(true)
                .displayTestDetails(true)
                .displayAttachedMedia(true)
                .build();
        pdfReporter.config(config);

        applyOptionalJsonConfig(pdfReporter);
        return pdfReporter;
    }

    private static void applyOptionalJsonConfig(ExtentPDFReporter pdfReporter) {
        try (InputStream json = PdfReportGenerator.class.getClassLoader().getResourceAsStream("pdf-config.json")) {
            if (json == null) {
                return;
            }
            File tempConfig = File.createTempFile("pdf-config", ".json");
            tempConfig.deleteOnExit();
            Files.copy(json, tempConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
            pdfReporter.loadJSONConfig(tempConfig);
        } catch (Exception ignored) {
            // JSON config is optional
        }
    }
}
