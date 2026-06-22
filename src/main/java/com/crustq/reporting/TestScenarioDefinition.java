package com.crustq.reporting;

import java.util.List;

/**
 * QA scenario metadata for module-wise and scenario PDF reports.
 */
public final class TestScenarioDefinition {

    private final String moduleCode;
    private final String moduleName;
    private final String scenarioId;
    private final String title;
    private final String type;
    private final String preconditions;
    private final List<String> steps;
    private final String expectedResult;

    public TestScenarioDefinition(
            String moduleCode,
            String moduleName,
            String scenarioId,
            String title,
            String type,
            String preconditions,
            List<String> steps,
            String expectedResult) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.scenarioId = scenarioId;
        this.title = title;
        this.type = type;
        this.preconditions = preconditions;
        this.steps = List.copyOf(steps);
        this.expectedResult = expectedResult;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getPreconditions() {
        return preconditions;
    }

    public List<String> getSteps() {
        return steps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String extentDisplayName() {
        return "[" + moduleName + "] " + scenarioId + " — " + title;
    }

    public String categoryTag() {
        return moduleCode + "-" + moduleName.replaceAll("[^A-Za-z0-9]+", "");
    }
}
