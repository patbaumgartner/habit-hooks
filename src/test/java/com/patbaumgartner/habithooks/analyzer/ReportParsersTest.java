package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ReportParsersTest {

    @TempDir
    Path tempDir;

    @Test
    void parsesSpotbugsBugInstances() throws Exception {
        Path report = write("target/spotbugsXml.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <BugCollection>
                  <BugInstance type="NP_NONNULL_RETURN_VIOLATION">
                    <LongMessage>Method may return null from non-null method</LongMessage>
                    <SourceLine sourcepath="src/main/java/com/example/Example.java" start="42"/>
                  </BugInstance>
                </BugCollection>
                """);

        List<Violation> violations = ReportParsers.spotbugsXml().parse(report, tempDir, "spotbugs");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("spotbugs:NP_NONNULL_RETURN_VIOLATION");
        assertThat(violations.get(0).location()).isEqualTo("src/main/java/com/example/Example.java:42");
    }

    @Test
    void parsesInvalidCyclonedxBomAsViolation() throws Exception {
        Path report = write("target/bom.json", "{\"bomFormat\":\"Other\"}");

        List<Violation> violations = ReportParsers.cyclonedxJson().parse(report, tempDir, "cyclonedx");

        assertThat(violations).extracting(Violation::ruleId)
            .containsExactly("cyclonedx:InvalidBom", "cyclonedx:MissingComponents");
    }

    @Test
    void parsesSurvivingPitMutations() throws Exception {
        Path report = write("target/pit-reports/mutations.xml", """
                <?xml version="1.0" encoding="UTF-8"?>
                <mutations>
                  <mutation detected="false" status="SURVIVED">
                    <sourceFile>Example.java</sourceFile>
                    <lineNumber>12</lineNumber>
                    <mutator>ConditionalsBoundaryMutator</mutator>
                    <description>changed conditional boundary</description>
                  </mutation>
                </mutations>
                """);

        List<Violation> violations = ReportParsers.pitestXml().parse(report, tempDir, "pitest");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("pitest:SURVIVED");
        assertThat(violations.get(0).location()).isEqualTo("Example.java:12");
    }

    @Test
    void parsesJacocoReportWithDoctype() throws Exception {
        Path report = write("target/site/jacoco/jacoco.xml", """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">
                <report name="example">
                  <counter type="LINE" missed="3" covered="7"/>
                </report>
                """);

        List<Violation> violations = ReportParsers.jacocoXml().parse(report, tempDir, "jacoco");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("jacoco:LineCoverage");
        assertThat(violations.get(0).message()).contains("70%", "7/10");
    }

    @Test
    void parsesErrorProneCompilerOutput() throws Exception {
        Path report = write("target/habit-hooks/errorprone.log", """
                [ERROR] Example.java:[12,8] [ReturnValueIgnored] Return value ignored
                """);

        List<Violation> violations = ReportParsers.errorProneText().parse(report, tempDir, "errorprone");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("errorprone:ReturnValueIgnored");
    }

    @Test
    void parsesOwaspDependencyCheckVulnerabilities() throws Exception {
        Path report = write("target/dependency-check-report.json", """
                {
                  "dependencies": [
                    {
                      "fileName": "library.jar",
                      "vulnerabilities": [
                        {
                          "name": "CVE-2026-0001",
                          "severity": "HIGH",
                          "cvssv3": { "baseScore": 8.1 },
                          "description": "Example vulnerability"
                        }
                      ]
                    }
                  ]
                }
                """);

        List<Violation> violations = ReportParsers.owaspDependencyCheckJson().parse(report, tempDir, "owasp");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).ruleId()).isEqualTo("owasp:CveHigh");
        assertThat(violations.get(0).message()).contains("CVE-2026-0001", "CVSS 8.1");
    }

    private Path write(String relativePath, String content) throws Exception {
        Path report = tempDir.resolve(relativePath);
        Files.createDirectories(report.getParent());
        Files.writeString(report, content);
        return report;
    }

}
