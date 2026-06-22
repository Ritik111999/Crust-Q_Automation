package com.crustq.reporting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalogue of QA scenarios. Register entries as automation scripts are added.
 */
public final class TestScenarioCatalog {

    private static final Map<String, TestScenarioDefinition> BY_KEY = new LinkedHashMap<>();

    static {
        register(
                "FrameworkSmokeTest#verifyFrameworkWiring",
                "00",
                "00 - Framework",
                "FW-001",
                "Framework wiring smoke check",
                "Positive",
                "Chrome browser available; config.properties on classpath",
                List.of("Launch Chrome via BaseTest", "Verify WebDriver thread binding", "Confirm framework initialization"),
                "WebDriver initializes and framework components load without error"
        );
    }

    private TestScenarioCatalog() {
    }

    public static TestScenarioDefinition lookup(String lookupKey) {
        return BY_KEY.get(lookupKey);
    }

    private static void register(
            String lookupKey,
            String moduleCode,
            String moduleName,
            String scenarioId,
            String title,
            String type,
            String preconditions,
            List<String> steps,
            String expectedResult) {
        BY_KEY.put(lookupKey, new TestScenarioDefinition(
                moduleCode, moduleName, scenarioId, title, type,
                preconditions, steps, expectedResult));
    }
}
