package com.crustq.reporting;

import java.time.Duration;
import java.time.Instant;

/**
 * Executed scenario with runtime status and timing.
 */
public final class TestScenarioResult {

    private final TestScenarioDefinition definition;
    private final String status;
    private final Instant startedAt;
    private final Instant finishedAt;
    private final String failureMessage;

    public TestScenarioResult(
            TestScenarioDefinition definition,
            String status,
            Instant startedAt,
            Instant finishedAt,
            String failureMessage) {
        this.definition = definition;
        this.status = status;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.failureMessage = failureMessage;
    }

    public TestScenarioDefinition getDefinition() {
        return definition;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Duration getDuration() {
        if (startedAt == null || finishedAt == null) {
            return Duration.ZERO;
        }
        return Duration.between(startedAt, finishedAt);
    }

    public boolean isPassed() {
        return "PASS".equalsIgnoreCase(status);
    }

    public boolean isFailed() {
        return "FAIL".equalsIgnoreCase(status);
    }

    public boolean isSkipped() {
        return "SKIP".equalsIgnoreCase(status);
    }
}
