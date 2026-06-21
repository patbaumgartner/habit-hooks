package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class MavenGoalAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void projectScopedAnalyzerDoesNotRequireFiles() {
        MavenGoalAnalyzer analyzer = analyzer("jacoco", "target/site/jacoco/jacoco.xml", ReportParsers.jacocoXml(), 0,
                "");

        assertThat(analyzer.requiresFiles()).isFalse();
    }

    @Test
    void orchestratorRunsProjectScopedAnalyzerWhenFileScopeIsEmpty() throws IOException {
        writeJacocoReport();
        MavenGoalAnalyzer analyzer = analyzer("jacoco", "target/site/jacoco/jacoco.xml", ReportParsers.jacocoXml(), 0,
                "");

        AnalysisResult result = new AnalysisOrchestrator(List.of(analyzer)).analyze(List.of(), tempDir);

        assertThat(result.violations()).extracting(Violation::ruleId).containsExactly("jacoco:LineCoverage");
    }

    @Test
    void missingReportAfterFailedGoalBecomesViolation() {
        MavenGoalAnalyzer analyzer = analyzer("spotbugs", "target/spotbugsXml.xml", ReportParsers.spotbugsXml(), 1,
                "failed");

        List<Violation> violations = analyzer.analyze(List.of(), tempDir);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("spotbugs:report-missing");
    }

    @Test
    void capturedOutputCanBeParsedAsFormatterFeedback() {
        MavenGoalAnalyzer analyzer = new CapturingAnalyzer("spring-javaformat", "target/habit-hooks/format.log",
                ReportParsers.springJavaFormatText(), 1, "Formatting violation in Example.java");

        List<Violation> violations = analyzer.analyze(List.of(), tempDir);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("spring-javaformat:Formatting");
    }

    @Test
    void successfulCapturedOutputRunIsCleanEvenWhenMavenPrintedLogs() {
        MavenGoalAnalyzer analyzer = new CapturingAnalyzer("spring-javaformat", "target/habit-hooks/format.log",
                ReportParsers.springJavaFormatText(), 0, "[INFO] BUILD SUCCESS");

        List<Violation> violations = analyzer.analyze(List.of(), tempDir);

        assertThat(violations).isEmpty();
    }

    private void writeJacocoReport() throws IOException {
        Path report = tempDir.resolve("target/site/jacoco/jacoco.xml");
        Files.createDirectories(report.getParent());
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8"?>
                <report name="habit-hooks">
                  <counter type="LINE" missed="3" covered="7"/>
                </report>
                """);
    }

    private static MavenGoalAnalyzer analyzer(String toolPrefix, String reportFile,
            MavenGoalAnalyzer.ReportParser parser, int exitCode, String output) {
        return new MavenGoalAnalyzer(toolPrefix, "verify", reportFile, parser) {
            @Override
            ExecutionResult runMaven(Path workingDir) {
                return new ExecutionResult(exitCode, output);
            }
        };
    }

    private static final class CapturingAnalyzer extends CapturingMavenGoalAnalyzer {

        private final int exitCode;

        private final String output;

        private CapturingAnalyzer(String toolPrefix, String reportFile, ReportParser parser, int exitCode,
                String output) {
            super(toolPrefix, "verify", reportFile, parser);
            this.exitCode = exitCode;
            this.output = output;
        }

        @Override
        ExecutionResult runMaven(Path workingDir) {
            return new ExecutionResult(exitCode, output);
        }

    }

}
