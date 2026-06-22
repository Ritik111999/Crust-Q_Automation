package com.crustq.reporting;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;

import java.time.Instant;

/**
 * Builds suite-level execution metrics from TestNG results.
 */
public final class ExecutionMetricsCollector {

    private ExecutionMetricsCollector() {
    }

    public static ExecutionMetrics collect(ISuite suite, Instant startedAt, Instant finishedAt) {
        int passed = 0;
        int failed = 0;
        int skipped = 0;

        for (ISuiteResult suiteResult : suite.getResults().values()) {
            ITestContext context = suiteResult.getTestContext();
            passed += context.getPassedTests().size();
            failed += context.getFailedTests().size();
            skipped += context.getSkippedTests().size();
        }

        return new ExecutionMetrics(passed, failed, skipped, startedAt, finishedAt);
    }
}
