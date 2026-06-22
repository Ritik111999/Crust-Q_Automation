package com.crustq.reporting;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

/**
 * Aggregated suite execution metrics.
 */
public final class ExecutionMetrics {

    private final int total;
    private final int passed;
    private final int failed;
    private final int skipped;
    private final Duration duration;

    public ExecutionMetrics(int passed, int failed, int skipped, Instant startedAt, Instant finishedAt) {
        this.passed = passed;
        this.failed = failed;
        this.skipped = skipped;
        this.total = passed + failed + skipped;
        this.duration = Duration.between(startedAt, finishedAt);
    }

    public int getTotal() {
        return total;
    }

    public int getPassed() {
        return passed;
    }

    public int getFailed() {
        return failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public String getFormattedDuration() {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%dh %dm %ds", hours, minutes, secs);
        }
        if (minutes > 0) {
            return String.format(Locale.US, "%dm %ds", minutes, secs);
        }
        return String.format(Locale.US, "%ds", secs);
    }

    public double getPassRate() {
        if (total == 0) {
            return 0.0;
        }
        return (passed * 100.0) / total;
    }
}
