package com.crustq.reporting;

import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Maps TestNG metadata to Extent category tags.
 */
public final class ScenarioCategoryMapper {

    private ScenarioCategoryMapper() {
    }

    public static List<String> resolveCategories(ITestResult result, TestScenarioDefinition scenario) {
        Set<String> categories = new LinkedHashSet<>();

        categories.add(resolveScenarioType(result));
        if (scenario != null) {
            categories.add(scenario.categoryTag());
        } else {
            categories.add(resolveFunctionalArea(result));
        }

        return new ArrayList<>(categories);
    }

    private static String resolveScenarioType(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        if (groups != null) {
            for (String group : groups) {
                if ("Positive".equalsIgnoreCase(group)) {
                    return "Positive_Scenario";
                }
                if ("Negative".equalsIgnoreCase(group)) {
                    return "Negative_Scenario";
                }
            }
        }
        return "Uncategorized_Scenario";
    }

    private static String resolveFunctionalArea(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();

        if (className.contains("User")) {
            return "User_Module";
        }
        if (className.contains("Admin")) {
            return "Admin_Module";
        }
        if (className.contains("Driver")) {
            return "Driver_Module";
        }
        return toTitleToken(className.replace("Test", ""));
    }

    private static String toTitleToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return "Framework";
        }
        return raw.substring(0, 1).toUpperCase(Locale.US) + raw.substring(1);
    }
}
