package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
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

    private final String toolPrefix;

    private final String reportFile;

    private final ReportParser reportParser;

    private final MavenProcessRunner processRunner;

    private final MavenAnalyzerViolations analyzerViolations;

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
        this.reportFile = reportFile;
        this.reportParser = reportParser;
        this.processRunner = new MavenProcessRunner(toolPrefix, goal);
        this.analyzerViolations = new MavenAnalyzerViolations(toolPrefix, goal, reportFile);
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
        return processRunner.isAvailable(workingDir);
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        ExecutionResult execution = runMaven(workingDir);
        Optional<Path> outputLog = writeCapturedOutput(workingDir, execution.output());
        if (isLifecycleBlocked(execution)) {
            return lifecycleBlockedViolation(execution, workingDir, outputLog);
        }
        if (capturesOutput() && execution.exitCode() == 0) {
            return List.of();
        }
        Path reportPath = workingDir.resolve(reportFile);
        if (!Files.isRegularFile(reportPath)) {
            return missingReportViolation(execution, workingDir, outputLog);
        }
        return parseReport(reportPath, workingDir, execution, outputLog);
    }

    ExecutionResult runMaven(Path workingDir) {
        return processRunner.run(workingDir);
    }

    private Optional<Path> writeCapturedOutput(Path workingDir, String output) {
        return new MavenOutputCapture(toolPrefix, reportFile, capturesOutput()).write(workingDir, output);
    }

    boolean capturesOutput() {
        return false;
    }

    private boolean isLifecycleBlocked(ExecutionResult execution) {
        return MavenExecutionClassifier.lifecycleBlocked(toolPrefix, execution.exitCode(), execution.output());
    }

    private List<Violation> parseReport(Path reportPath, Path workingDir, ExecutionResult execution,
            Optional<Path> outputLog) {
        try {
            List<Violation> violations = reportParser.parse(reportPath, workingDir, toolPrefix);
            if (execution.exitCode() != 0 && violations.isEmpty()) {
                return analyzerViolations.goalFailed(execution, workingDir, outputLog);
            }
            return violations;
        }
        catch (IOException | ParserConfigurationException | SAXException e) {
            LOGGER.error("Failed to parse {} report {}: {}", toolPrefix, reportPath, e.getMessage(), e);
            return List.of(analyzerViolations.unreadableReport(e));
        }
    }

    private List<Violation> missingReportViolation(ExecutionResult execution, Path workingDir,
            Optional<Path> outputLog) {
        return analyzerViolations.missingReport(execution, workingDir, outputLog);
    }

    private List<Violation> lifecycleBlockedViolation(ExecutionResult execution, Path workingDir,
            Optional<Path> outputLog) {
        return analyzerViolations.lifecycleBlocked(execution, workingDir, outputLog);
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

}
