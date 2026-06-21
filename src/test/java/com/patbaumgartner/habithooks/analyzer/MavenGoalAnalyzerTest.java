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
    void missingReportAfterFailedGoalBecomesViolation() throws IOException {
        MavenGoalAnalyzer analyzer = analyzer("spotbugs", "target/spotbugsXml.xml", ReportParsers.spotbugsXml(), 1,
                "[ERROR] Compilation failure");

        List<Violation> violations = analyzer.analyze(List.of(), tempDir);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("spotbugs:report-missing");
        assertThat(violations.get(0).message()).contains("Last Maven output: [ERROR] Compilation failure");
        assertThat(violations.get(0).message()).contains("target/habit-hooks/spotbugs.log");
        assertThat(Files.readString(tempDir.resolve("target/habit-hooks/spotbugs.log")))
            .contains("Compilation failure");
    }

    @Test
    void failedGoalWithEmptyParsedReportIncludesMavenOutput() throws IOException {
        writeJacocoReport(0, 10);
        MavenGoalAnalyzer analyzer = analyzer("jacoco", "target/site/jacoco/jacoco.xml", ReportParsers.jacocoXml(), 1,
                """
                        [ERROR] Failed to compile ArchitectureTest.java
                        [ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoFailureException
                        """);

        List<Violation> violations = analyzer.analyze(List.of(), tempDir);

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("jacoco:goal-failed");
        assertThat(violations.get(0).message()).contains("ArchitectureTest.java");
        assertThat(violations.get(0).message()).contains("target/habit-hooks/jacoco.log");
        assertThat(violations.get(0).message()).doesNotContain("cwiki.apache.org");
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
        writeJacocoReport(3, 7);
    }

    private void writeJacocoReport(int missed, int covered) throws IOException {
        Path report = tempDir.resolve("target/site/jacoco/jacoco.xml");
        Files.createDirectories(report.getParent());
        Files.writeString(report, """
                <?xml version="1.0" encoding="UTF-8"?>
                <report name="habit-hooks">
                                    <counter type="LINE" missed="%d" covered="%d"/>
                </report>
                                """.formatted(missed, covered));
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
