package com.crustq.reporting;

import com.crustq.config.ConfigReader;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the Module Wise QA Report as PDF, grouped by User / Admin / Driver modules.
 */
public final class ModuleWiseReportGenerator {

    private ModuleWiseReportGenerator() {
    }

    public static void generate(List<TestScenarioResult> results, ReportArtifactPaths paths, Instant generatedAt) {
        String productName = ConfigReader.get("report.product.name", "Crust & Q PWA");
        String title = productName + " — Module Wise QA Report";

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

        appendModuleSummary(html, results);
        appendScenarioDetails(html, results);
        html.append("</body></html>");

        HtmlToPdfConverter.writePdf(html.toString(), paths.getModuleWisePdfPath());
    }

    private static void appendModuleSummary(StringBuilder html, List<TestScenarioResult> results) {
        html.append("<h2>Module Summary</h2>");
        html.append("<table><thead><tr>")
                .append("<th>Module</th><th>Total</th><th>Passed</th><th>Failed</th><th>Result</th>")
                .append("</tr></thead><tbody>");

        Map<String, ModuleStats> statsByModule = new LinkedHashMap<>();
        for (TestScenarioResult result : results) {
            String moduleName = result.getDefinition().getModuleName();
            statsByModule.computeIfAbsent(moduleName, ModuleStats::new).add(result);
        }

        for (ModuleStats stats : statsByModule.values()) {
            String moduleResult = stats.failed == 0 ? "PASS" : "FAIL";
            String resultClass = stats.failed == 0 ? "module-pass" : "module-fail";
            html.append("<tr><td>").append(QaReportHtmlSupport.escape(stats.moduleName))
                    .append("</td><td>").append(stats.total)
                    .append("</td><td>").append(stats.passed)
                    .append("</td><td>").append(stats.failed)
                    .append("</td><td class=\"").append(resultClass).append("\">")
                    .append(moduleResult).append("</td></tr>");
        }

        html.append("</tbody></table>");
    }

    private static void appendScenarioDetails(StringBuilder html, List<TestScenarioResult> results) {
        html.append("<h2>Scenario Details</h2>");

        String currentModule = null;
        for (TestScenarioResult result : results) {
            String moduleName = result.getDefinition().getModuleName();
            if (!moduleName.equals(currentModule)) {
                if (currentModule != null) {
                    html.append("</tbody></table>");
                }
                currentModule = moduleName;
                html.append("<h3>").append(QaReportHtmlSupport.escape(moduleName)).append("</h3>");
                html.append("<table><thead><tr>")
                        .append("<th>Scenario ID</th><th>Title</th><th>Type</th><th>Expected Result</th><th>Status</th>")
                        .append("</tr></thead><tbody>");
            }

            TestScenarioDefinition def = result.getDefinition();
            html.append("<tr><td>").append(QaReportHtmlSupport.escape(def.getScenarioId()))
                    .append("</td><td>").append(QaReportHtmlSupport.escape(def.getTitle()))
                    .append("</td><td>").append(QaReportHtmlSupport.escape(def.getType()))
                    .append("</td><td>").append(QaReportHtmlSupport.escape(def.getExpectedResult()))
                    .append("</td><td class=\"")
                    .append(QaReportHtmlSupport.statusClass(result.getStatus())).append("\">")
                    .append(QaReportHtmlSupport.escape(result.getStatus().toUpperCase()))
                    .append("</td></tr>");
        }
        if (currentModule != null) {
            html.append("</tbody></table>");
        }
    }

    private static final class ModuleStats {
        private final String moduleName;
        private int total;
        private int passed;
        private int failed;

        private ModuleStats(String moduleName) {
            this.moduleName = moduleName;
        }

        private void add(TestScenarioResult result) {
            total++;
            if (result.isPassed()) {
                passed++;
            } else if (result.isFailed()) {
                failed++;
            }
        }
    }
}
