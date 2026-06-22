package com.crustq.reporting;

import com.crustq.config.ConfigReader;

import java.time.Instant;
import java.util.List;

/**
 * Generates the Test Scenario Report as PDF with steps and expected results.
 */
public final class ScenarioReportGenerator {

    private ScenarioReportGenerator() {
    }

    public static void generate(List<TestScenarioResult> results, ReportArtifactPaths paths, Instant generatedAt) {
        String productName = ConfigReader.get("report.product.name", "Crust & Q PWA");
        String title = productName + " — Test Scenario Report";

        int passed = (int) results.stream().filter(TestScenarioResult::isPassed).count();
        int failed = (int) results.stream().filter(TestScenarioResult::isFailed).count();
        int skipped = (int) results.stream().filter(TestScenarioResult::isSkipped).count();
        int total = results.size();

        StringBuilder html = new StringBuilder();
        html.append(QaReportHtmlSupport.documentStart(title));
        html.append("<h1>").append(QaReportHtmlSupport.escape(title)).append("</h1>");
        html.append("<div class=\"meta\">Generated: ")
                .append(QaReportHtmlSupport.generatedTimestamp(generatedAt)).append("</div>");
        html.append("<div class=\"summary\">Total Scenarios: ").append(total)
                .append(" | Passed: ").append(passed)
                .append(" | Failed: ").append(failed);
        if (skipped > 0) {
            html.append(" | Skipped: ").append(skipped);
        }
        html.append("</div>");
        html.append("<h2>Scenario Details</h2>");

        for (TestScenarioResult result : results) {
            appendScenarioCard(html, result);
        }

        html.append("</body></html>");
        HtmlToPdfConverter.writePdf(html.toString(), paths.getScenarioPdfPath());
    }

    private static void appendScenarioCard(StringBuilder html, TestScenarioResult result) {
        TestScenarioDefinition def = result.getDefinition();
        String statusClass = QaReportHtmlSupport.statusClass(result.getStatus());

        html.append("<div class=\"scenario-card\">");
        html.append("<div class=\"scenario-title\">")
                .append(QaReportHtmlSupport.escape(def.getScenarioId())).append(" — ")
                .append(QaReportHtmlSupport.escape(def.getTitle())).append(" ")
                .append("<span class=\"").append(statusClass).append("\">")
                .append(QaReportHtmlSupport.escape(def.getType())).append("</span></div>");

        html.append("<div class=\"scenario-meta\">Module: ")
                .append(QaReportHtmlSupport.escape(def.getModuleName()))
                .append(" | Type: ")
                .append(QaReportHtmlSupport.escape(def.getType())).append("</div>");

        html.append("<div class=\"scenario-block\"><strong>Preconditions:</strong> ")
                .append(QaReportHtmlSupport.escape(def.getPreconditions())).append("</div>");

        html.append("<div class=\"scenario-block\"><strong>Test Steps:</strong><ol>");
        for (String step : def.getSteps()) {
            html.append("<li>").append(QaReportHtmlSupport.escape(step)).append("</li>");
        }
        html.append("</ol></div>");

        html.append("<div class=\"scenario-block\"><strong>Expected Result:</strong> ")
                .append(QaReportHtmlSupport.escape(def.getExpectedResult())).append("</div>");

        html.append("<div class=\"scenario-block\"><strong>Actual Status:</strong> ")
                .append("<span class=\"").append(statusClass).append("\">")
                .append(QaReportHtmlSupport.escape(result.getStatus().toUpperCase()))
                .append("</span>");
        if (result.isFailed() && result.getFailureMessage() != null) {
            html.append("<br/><strong>Failure:</strong> ")
                    .append(QaReportHtmlSupport.escape(result.getFailureMessage()));
        }
        html.append("</div></div>");
    }
}
