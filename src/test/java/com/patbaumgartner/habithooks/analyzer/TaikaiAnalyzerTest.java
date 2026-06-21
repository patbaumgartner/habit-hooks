package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class TaikaiAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void toolPrefixIsTaikai() {
        assertThat(new TaikaiAnalyzer("ArchitectureTest").toolPrefix()).isEqualTo("taikai");
    }

    @Test
    void isNotAvailableWhenTestSourceDirMissing() {
        assertThat(new TaikaiAnalyzer("ArchitectureTest").isAvailable(tempDir)).isFalse();
    }

    @Test
    void isNotAvailableWhenTestClassMissing() throws IOException {
        writeTestClass("OtherTest.java", "class OtherTest {}");
        writeMavenWrapper();
        assertThat(new TaikaiAnalyzer("ArchitectureTest").isAvailable(tempDir)).isFalse();
    }

    @Test
    void isAvailableWhenMvnwAndTestClassPresent() throws IOException {
        writeTestClass("ArchitectureTest.java", "class ArchitectureTest {}");
        writeMavenWrapper();
        assertThat(new TaikaiAnalyzer("ArchitectureTest").isAvailable(tempDir)).isTrue();
    }

    @Test
    void isNotAvailableWhenTaikaiTestHasNoBuildDependency() throws IOException {
        writeTestClass("ArchitectureTest.java", """
                import com.enofex.taikai.Taikai;
                class ArchitectureTest {}
                """);
        writeMavenWrapper();
        Files.writeString(tempDir.resolve("pom.xml"), "<project></project>");

        assertThat(new TaikaiAnalyzer("ArchitectureTest").isAvailable(tempDir)).isFalse();
    }

    @Test
    void isAvailableWhenTaikaiTestHasBuildDependency() throws IOException {
        writeTestClass("ArchitectureTest.java", """
                import com.enofex.taikai.Taikai;
                class ArchitectureTest {}
                """);
        writeMavenWrapper();
        Files.writeString(tempDir.resolve("pom.xml"), "<artifactId>taikai</artifactId>");

        assertThat(new TaikaiAnalyzer("ArchitectureTest").isAvailable(tempDir)).isTrue();
    }

    @Test
    void parsesCleanSurefireReportAsNoViolations() throws IOException {
        writeSurefireReport(tempDir, "ArchitectureTest", cleanReport());
        assertThat(noMavenAnalyzer("ArchitectureTest").analyze(List.of(), tempDir)).isEmpty();
    }

    @Test
    void parsesFailedSurefireReportAsViolations() throws IOException {
        writeSurefireReport(tempDir, "ArchitectureTest", failedReport());
        List<Violation> violations = noMavenAnalyzer("ArchitectureTest").analyze(List.of(), tempDir);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("taikai:shouldFulfillArchitectureConstraints");
        assertThat(violations.get(0).message()).contains("Architecture Violation");
        assertThat(violations.get(0).line()).isEqualTo(1);
    }

    @Test
    void parsesErrorElementAsViolation() throws IOException {
        writeSurefireReport(tempDir, "ArchitectureTest", errorReport());
        List<Violation> violations = noMavenAnalyzer("ArchitectureTest").analyze(List.of(), tempDir);
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("taikai:shouldFulfillArchitectureConstraints");
    }

    @Test
    void returnsEmptyWhenNoSurefireDirectory() {
        assertThat(noMavenAnalyzer("ArchitectureTest").analyze(List.of(), tempDir)).isEmpty();
    }

    @Test
    void ignoresReportsThatDontMatchTestClass() throws IOException {
        writeSurefireReport(tempDir, "OtherTest", failedReport().replace("ArchitectureTest", "OtherTest"));
        assertThat(noMavenAnalyzer("ArchitectureTest").analyze(List.of(), tempDir)).isEmpty();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void writeTestClass(String filename, String content) throws IOException {
        Path testDir = tempDir.resolve("src/test/java/com/example");
        Files.createDirectories(testDir);
        Files.writeString(testDir.resolve(filename), content);
    }

    private void writeMavenWrapper() throws IOException {
        Files.writeString(tempDir.resolve("mvnw"), "#!/bin/sh");
    }

    private static TaikaiAnalyzer noMavenAnalyzer(String testClass) {
        return new TaikaiAnalyzer(testClass) {
            @Override
            int runMaven(Path workingDir) {
                // no-op: Surefire report is pre-populated by the test
                return 0;
            }
        };
    }

    private static void writeSurefireReport(Path tempDir, String testClass, String content) throws IOException {
        Path surefireDir = tempDir.resolve("target/surefire-reports");
        Files.createDirectories(surefireDir);
        Files.writeString(surefireDir.resolve("TEST-com.example." + testClass + ".xml"), content);
    }

    private static String cleanReport() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="com.example.ArchitectureTest" tests="1" failures="0" errors="0">
                  <testcase name="shouldFulfillArchitectureConstraints"
                            classname="com.example.ArchitectureTest" time="0.5"/>
                </testsuite>
                """;
    }

    private static String failedReport() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="com.example.ArchitectureTest" tests="1" failures="1" errors="0">
                  <testcase name="shouldFulfillArchitectureConstraints"
                            classname="com.example.ArchitectureTest" time="0.5">
                    <failure message="Architecture Violation in rule &apos;no cycles&apos;">
                      Full stack trace here.
                    </failure>
                  </testcase>
                </testsuite>
                """;
    }

    private static String errorReport() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <testsuite name="com.example.ArchitectureTest" tests="1" failures="0" errors="1">
                  <testcase name="shouldFulfillArchitectureConstraints"
                            classname="com.example.ArchitectureTest" time="0.1">
                    <error message="Architecture Violation: unexpected error">
                      java.lang.AssertionError: ...
                    </error>
                  </testcase>
                </testsuite>
                """;
    }

}
