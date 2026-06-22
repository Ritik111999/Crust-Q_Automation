package com.crustq.reporting;

import com.crustq.config.ConfigReader;

import java.io.File;

/**
 * Resolved PDF report artifact paths for the current execution run.
 */
public final class ReportArtifactPaths {

    private final String reportDirectory;
    private final String extentHtmlTempPath;
    private final String extentPdfPath;
    private final String moduleWisePdfPath;
    private final String scenarioPdfPath;

    public ReportArtifactPaths(String reportDirectory, String extentHtmlTempPath, String extentPdfPath) {
        this.reportDirectory = reportDirectory;
        this.extentHtmlTempPath = extentHtmlTempPath;
        this.extentPdfPath = extentPdfPath;

        String productName = ConfigReader.get("report.product.name", "Crust & Q PWA");
        this.moduleWisePdfPath = reportDirectory + File.separator + productName + " — Module Wise QA Report.pdf";
        this.scenarioPdfPath = reportDirectory + File.separator + productName + " — Test Scenario Report.pdf";
    }

    public String getReportDirectory() {
        return reportDirectory;
    }

    public String getExtentHtmlTempPath() {
        return extentHtmlTempPath;
    }

    public String getExtentPdfPath() {
        return extentPdfPath;
    }

    public String getModuleWisePdfPath() {
        return moduleWisePdfPath;
    }

    public String getScenarioPdfPath() {
        return scenarioPdfPath;
    }
}
