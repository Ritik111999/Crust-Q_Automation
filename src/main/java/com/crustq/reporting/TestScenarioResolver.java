package com.crustq.reporting;

import com.crustq.config.ApplicationRole;
import com.crustq.config.ConfigReader;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Resolves scenario catalogue entries (or fallbacks) from TestNG execution metadata.
 */
public final class TestScenarioResolver {

    private TestScenarioResolver() {
    }

    public static TestScenarioDefinition resolve(ITestResult result) {
        String lookupKey = buildLookupKey(result);
        TestScenarioDefinition catalogEntry = TestScenarioCatalog.lookup(lookupKey);
        if (catalogEntry != null) {
            return catalogEntry;
        }

        String className = result.getTestClass().getRealClass().getSimpleName();
        String methodName = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        String title = description != null && !description.isBlank() ? description : methodName;
        String type = resolveType(result);
        ModuleInfo module = resolveModule(className);
        String scenarioId = buildFallbackScenarioId(module.prefix(), methodName, result.getParameters());

        return new TestScenarioDefinition(
                module.code(),
                module.name(),
                scenarioId,
                title,
                type,
                "See test implementation",
                List.of("Execute automated test steps for: " + title),
                "Test completes without assertion failures"
        );
    }

    public static String buildLookupKey(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        String methodName = result.getMethod().getMethodName();
        StringBuilder key = new StringBuilder(className).append('#').append(methodName);

        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0 && parameters[0] != null) {
            key.append('#').append(parameters[0].toString());
        }
        return key.toString();
    }

    public static String buildResultKey(ITestResult result) {
        String base = buildLookupKey(result);
        Object[] parameters = result.getParameters();
        if (parameters == null || parameters.length <= 1) {
            return base;
        }
        return base + "#" + Arrays.deepHashCode(parameters);
    }

    private static String resolveType(ITestResult result) {
        String[] groups = result.getMethod().getGroups();
        if (groups != null) {
            for (String group : groups) {
                if ("Negative".equalsIgnoreCase(group)) {
                    return "Negative";
                }
                if ("Positive".equalsIgnoreCase(group)) {
                    return "Positive";
                }
            }
        }
        return "Positive";
    }

    private static ModuleInfo resolveModule(String className) {
        if (className.contains("User") || className.startsWith("Customer")) {
            return moduleFromRole(ApplicationRole.USER);
        }
        if (className.contains("Admin") || className.contains("Dispatcher")) {
            return moduleFromRole(ApplicationRole.ADMIN);
        }
        if (className.contains("Driver") || className.contains("Delivery")) {
            return moduleFromRole(ApplicationRole.DRIVER);
        }
        return new ModuleInfo("00", "00 - Framework", "FW");
    }

    private static ModuleInfo moduleFromRole(ApplicationRole role) {
        return new ModuleInfo(role.getModuleCode(), role.getModuleName(), role.name());
    }

    private static String buildFallbackScenarioId(String prefix, String methodName, Object[] parameters) {
        if (parameters != null && parameters.length > 0 && parameters[0] != null) {
            String suffix = parameters[0].toString()
                    .replaceAll("[^A-Za-z0-9]+", "")
                    .toUpperCase(Locale.US);
            if (!suffix.isBlank()) {
                return prefix + "-AUTO-" + suffix.substring(0, Math.min(8, suffix.length()));
            }
        }
        return prefix + "-AUTO-" + methodName.substring(Math.max(0, methodName.length() - 8)).toUpperCase(Locale.US);
    }

    private record ModuleInfo(String code, String name, String prefix) {
    }
}
