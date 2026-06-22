package com.crustq.reporting;

import org.testng.ITestResult;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe collector for executed scenario results.
 */
public final class TestScenarioCollector {

    private static final Map<String, Instant> START_TIMES = new ConcurrentHashMap<>();
    private static final List<TestScenarioResult> RESULTS = new CopyOnWriteArrayList<>();

    private TestScenarioCollector() {
    }

    public static void reset() {
        START_TIMES.clear();
        RESULTS.clear();
    }

    public static void markStart(ITestResult result) {
        START_TIMES.put(TestScenarioResolver.buildResultKey(result), Instant.now());
    }

    public static void record(ITestResult result, String status) {
        String resultKey = TestScenarioResolver.buildResultKey(result);
        Instant startedAt = START_TIMES.getOrDefault(resultKey, Instant.now());
        Instant finishedAt = Instant.now();
        START_TIMES.remove(resultKey);

        TestScenarioDefinition definition = TestScenarioResolver.resolve(result);
        String failureMessage = null;
        if (result.getThrowable() != null) {
            failureMessage = result.getThrowable().getMessage();
        }

        RESULTS.add(new TestScenarioResult(definition, status, startedAt, finishedAt, failureMessage));
    }

    public static List<TestScenarioResult> getResults() {
        List<TestScenarioResult> sorted = new ArrayList<>(RESULTS);
        sorted.sort(Comparator
                .comparing((TestScenarioResult r) -> r.getDefinition().getModuleCode())
                .thenComparing(r -> r.getDefinition().getScenarioId()));
        return sorted;
    }
}
