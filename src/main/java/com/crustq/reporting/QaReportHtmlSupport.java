package com.crustq.reporting;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Shared HTML/CSS helpers for QA PDF reports.
 */
final class QaReportHtmlSupport {

    private static final DateTimeFormatter GENERATED_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private QaReportHtmlSupport() {
    }

    static String generatedTimestamp(Instant instant) {
        return GENERATED_FORMAT.format(instant);
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String statusClass(String status) {
        if (status == null) {
            return "status-skip";
        }
        return switch (status.toUpperCase()) {
            case "PASS" -> "status-pass";
            case "FAIL" -> "status-fail";
            default -> "status-skip";
        };
    }

    static String documentStart(String title) {
        return "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/>"
                + "<title>" + escape(title) + "</title>"
                + "<style>" + sharedStyles() + "</style></head><body>";
    }

    static String sharedStyles() {
        return """
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                       margin: 32px; color: #1f2937; background: #f8fafc; line-height: 1.5; }
                h1 { margin: 0 0 8px; font-size: 28px; color: #0f172a; }
                h2 { margin: 32px 0 12px; font-size: 20px; color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 6px; }
                h3 { margin: 24px 0 10px; font-size: 17px; color: #334155; }
                .meta { color: #64748b; margin-bottom: 20px; }
                .summary { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 16px 20px;
                           margin-bottom: 24px; font-size: 15px; }
                table { width: 100%; border-collapse: collapse; background: #fff; border: 1px solid #e2e8f0;
                        border-radius: 8px; overflow: hidden; margin-bottom: 20px; }
                th { background: #0f172a; color: #fff; text-align: left; padding: 10px 12px; font-size: 13px; }
                td { padding: 9px 12px; border-top: 1px solid #e2e8f0; font-size: 13px; vertical-align: top; }
                tr:nth-child(even) td { background: #f8fafc; }
                .status-pass { color: #15803d; font-weight: 700; }
                .status-fail { color: #b91c1c; font-weight: 700; }
                .status-skip { color: #b45309; font-weight: 700; }
                .module-pass { color: #15803d; font-weight: 700; }
                .module-fail { color: #b91c1c; font-weight: 700; }
                .scenario-card { background: #fff; border: 1px solid #e2e8f0; border-radius: 8px;
                                 padding: 16px 18px; margin-bottom: 16px; page-break-inside: avoid; }
                .scenario-title { font-size: 16px; font-weight: 700; margin-bottom: 8px; }
                .scenario-meta { color: #475569; font-size: 13px; margin-bottom: 8px; }
                .scenario-block { margin: 8px 0; font-size: 13px; }
                .scenario-block strong { color: #0f172a; }
                """;
    }
}
