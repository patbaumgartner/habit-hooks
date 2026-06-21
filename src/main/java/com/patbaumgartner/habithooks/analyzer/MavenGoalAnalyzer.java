package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Project-scoped analyzer that runs a Maven goal and normalizes the resulting report into
 * habit-hooks violations.
 */
public non-sealed class MavenGoalAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenGoalAnalyzer.class);

    private static final String MAVEN_WRAPPER = "mvnw";

    private final String toolPrefix;

    private final String goal;

    private final String reportFile;

    private final ReportParser reportParser;

    /**
     * Creates a Maven-backed analyzer.
     * @param toolPrefix prefix for normalized rule IDs
     * @param goal Maven goal or phase to execute
     * @param reportFile report file to parse after the goal runs, relative to project
     * root
     * @param reportParser parser for the configured report format
     */
    public MavenGoalAnalyzer(String toolPrefix, String goal, String reportFile, ReportParser reportParser) {
        this.toolPrefix = toolPrefix;
        this.goal = goal;
        this.reportFile = reportFile;
        this.reportParser = reportParser;
    }

    @Override
    public String toolPrefix() {
        return toolPrefix;
    }

    @Override
    public boolean requiresFiles() {
        return false;
    }

    @Override
    public boolean isAvailable(Path workingDir) {
        return Files.isRegularFile(workingDir.resolve(MAVEN_WRAPPER)) || commandExists("mvn");
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        ExecutionResult execution = runMaven(workingDir);
        writeCapturedOutput(workingDir, execution.output());
        if (capturesOutput() && execution.exitCode() == 0) {
            return List.of();
        }
        Path reportPath = workingDir.resolve(reportFile);
        if (!Files.isRegularFile(reportPath)) {
            return missingReportViolation(execution.exitCode());
        }
        return parseReport(reportPath, workingDir, execution.exitCode());
    }

    ExecutionResult runMaven(Path workingDir) {
        List<String> command = buildMavenCommand(workingDir);
        LOGGER.debug("Running {} analyzer: {}", toolPrefix, command);
        try {
            Process process = startProcess(command, workingDir);
            String output = readOutput(process);
            return new ExecutionResult(process.waitFor(), output);
        }
        catch (IOException | InterruptedException ex) {
            LOGGER.error("Failed to run {} Maven goal '{}': {}", toolPrefix, goal, ex.getMessage(), ex);
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new ExecutionResult(-1, ex.getMessage());
        }
    }

    private static Process startProcess(List<String> command, Path workingDir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private static String readOutput(Process process) throws IOException {
        try (InputStream out = process.getInputStream()) {
            return new String(out.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void writeCapturedOutput(Path workingDir, String output) {
        if (!capturesOutput()) {
            return;
        }
        Path reportPath = workingDir.resolve(reportFile);
        try {
            Files.createDirectories(reportPath.getParent());
            Files.writeString(reportPath, output, StandardCharsets.UTF_8);
        }
        catch (IOException ex) {
            LOGGER.error("Could not write {} output report {}: {}", toolPrefix, reportPath, ex.getMessage(), ex);
        }
    }

    boolean capturesOutput() {
        return false;
    }

    private List<String> buildMavenCommand(Path workingDir) {
        String mvn = Files.isRegularFile(workingDir.resolve(MAVEN_WRAPPER)) ? "./" + MAVEN_WRAPPER : "mvn";
        List<String> command = new ArrayList<>();
        command.add(mvn);
        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        command.addAll(List.of(goal.split("\\s+")));
        return List.copyOf(command);
    }

    private List<Violation> parseReport(Path reportPath, Path workingDir, int exitCode) {
        try {
            List<Violation> violations = reportParser.parse(reportPath, workingDir, toolPrefix);
            if (exitCode != 0 && violations.isEmpty()) {
                return List.of(buildViolation("goal-failed", reportFile, 1,
                        "Maven goal '" + goal + "' failed but the report contained no parseable findings."));
            }
            return violations;
        }
        catch (IOException | ParserConfigurationException | SAXException e) {
            LOGGER.error("Failed to parse {} report {}: {}", toolPrefix, reportPath, e.getMessage(), e);
            return List.of(buildViolation("report-unreadable", reportFile, 1,
                    "Could not parse " + toolPrefix + " report: " + e.getMessage()));
        }
    }

    private List<Violation> missingReportViolation(int exitCode) {
        if (exitCode == 0) {
            return List.of();
        }
        return List.of(buildViolation("report-missing", reportFile, 1,
                "Maven goal '" + goal + "' failed and did not produce " + reportFile + "."));
    }

    private Violation buildViolation(String rule, String file, int line, String message) {
        return new Violation(toolPrefix + ":" + rule, file, line, message);
    }

    private static boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").redirectErrorStream(true).start();
            try (InputStream out = process.getInputStream()) {
                out.transferTo(OutputStream.nullOutputStream());
            }
            return process.waitFor() == 0;
        }
        catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /** Parser for Maven-generated report files. */
    @FunctionalInterface
    public interface ReportParser {

        /**
         * Parses a report file into normalized violations.
         * @param reportPath report file path
         * @param workingDir project root
         * @param toolPrefix tool prefix for rule IDs
         * @return normalized violations
         * @throws IOException on file IO failure
         * @throws ParserConfigurationException on XML parser setup failure
         * @throws SAXException on XML parsing failure
         */
        List<Violation> parse(Path reportPath, Path workingDir, String toolPrefix)
                throws IOException, ParserConfigurationException, SAXException;

    }

    record ExecutionResult(int exitCode, String output) {
    }

}
