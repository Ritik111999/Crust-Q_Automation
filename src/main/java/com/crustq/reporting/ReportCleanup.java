package com.crustq.reporting;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

/**
 * Removes transient HTML artifacts after PDF reports are generated.
 */
public final class ReportCleanup {

    private ReportCleanup() {
    }

    public static void cleanupAfterRun(ReportArtifactPaths paths) {
        if (paths == null) {
            return;
        }
        deleteIfExists(Path.of(paths.getExtentHtmlTempPath()));
        deleteSparkFolder(Path.of(paths.getReportDirectory(), "spark"));
        deleteLegacyArtifacts(Path.of(paths.getReportDirectory()));
    }

    private static void deleteLegacyArtifacts(Path reportDir) {
        try (Stream<Path> files = Files.list(reportDir)) {
            files.filter(ReportCleanup::isLegacyArtifact).forEach(ReportCleanup::deleteIfExists);
        } catch (IOException ignored) {
            // Best-effort cleanup
        }
    }

    private static boolean isLegacyArtifact(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".html") || name.startsWith(".extent-temp-");
    }

    private static void deleteSparkFolder(Path sparkDir) {
        if (!Files.exists(sparkDir)) {
            return;
        }
        try {
            Files.walkFileTree(sparkDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
            // Best-effort cleanup
        }
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Best-effort cleanup
        }
    }
}
