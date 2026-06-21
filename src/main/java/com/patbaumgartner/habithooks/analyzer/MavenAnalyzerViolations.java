package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class MavenAnalyzerViolations {

    private final String toolPrefix;

    private final String goal;

    private final String reportFile;

    MavenAnalyzerViolations(String toolPrefix, String goal, String reportFile) {
        this.toolPrefix = toolPrefix;
        this.goal = goal;
        this.reportFile = reportFile;
    }

    List<Violation> goalFailed(ExecutionResult execution, Path workingDir, Optional<Path> outputLog) {
        return List.of(build("goal-failed", reportFile, 1,
                "Maven goal '" + goal + "' failed but the report contained no parseable findings."
                        + MavenOutputSummary.summarize(execution.output(), workingDir, outputLog)));
    }

    List<Violation> missingReport(ExecutionResult execution, Path workingDir, Optional<Path> outputLog) {
        if (execution.exitCode() == 0) {
            return List.of();
        }
        return List.of(build("report-missing", reportFile, 1, "Maven goal '" + goal + "' failed and did not produce "
                + reportFile + "." + MavenOutputSummary.summarize(execution.output(), workingDir, outputLog)));
    }

    List<Violation> lifecycleBlocked(ExecutionResult execution, Path workingDir, Optional<Path> outputLog) {
        return List.of(build("lifecycle-blocked", "pom.xml", 1, "Maven stopped before the " + toolPrefix
                + " analyzer goal started." + MavenOutputSummary.summarize(execution.output(), workingDir, outputLog)));
    }

    Violation unreadableReport(Exception ex) {
        return build("report-unreadable", reportFile, 1,
                "Could not parse " + toolPrefix + " report: " + ex.getMessage());
    }

    private Violation build(String rule, String file, int line, String message) {
        return new Violation(toolPrefix + ":" + rule, file, line, message);
    }

}
