package com.patbaumgartner.habithooks.analyzer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MavenOutputCapture {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenOutputCapture.class);

    private final String toolPrefix;

    private final String reportFile;

    private final boolean capturesOutput;

    MavenOutputCapture(String toolPrefix, String reportFile, boolean capturesOutput) {
        this.toolPrefix = toolPrefix;
        this.reportFile = reportFile;
        this.capturesOutput = capturesOutput;
    }

    Optional<Path> write(Path workingDir, String output) {
        if (skips(output)) {
            return Optional.empty();
        }
        Path reportPath = path(workingDir);
        try {
            createParentDirectories(reportPath);
            Files.writeString(reportPath, output, StandardCharsets.UTF_8);
            return Optional.of(reportPath);
        }
        catch (IOException ex) {
            LOGGER.error("Could not write {} output report {}: {}", toolPrefix, reportPath, ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private boolean skips(String output) {
        return !capturesOutput && output.isBlank();
    }

    private Path path(Path workingDir) {
        if (capturesOutput) {
            return workingDir.resolve(reportFile);
        }
        return outputDirectory(workingDir).resolve(toolPrefix + ".log");
    }

    private static Path outputDirectory(Path workingDir) {
        Path reportDir = workingDir.resolve("target/habit-hooks");
        if (Files.exists(reportDir) && !Files.isDirectory(reportDir)) {
            return workingDir.resolve("target/habit-hooks-logs");
        }
        return reportDir;
    }

    private static void createParentDirectories(Path reportPath) throws IOException {
        Path parent = reportPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

}
