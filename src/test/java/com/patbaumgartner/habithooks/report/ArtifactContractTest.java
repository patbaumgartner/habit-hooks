package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import com.patbaumgartner.habithooks.tasks.AgentTaskExporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactContractTest {

    @TempDir
    Path tempDir;

    @Test
    void reportJsonShapeIsStable() throws Exception {
        QualityReport report = fixedReport();

        Path output = new QualityReportWriter().write(report, tempDir, ReportFormat.JSON, Optional.empty());

        assertThat(Files.readString(output)).isEqualTo("""
                {
                  "generatedAt" : "2026-06-21T00:00:00Z",
                  "filesChecked" : 1,
                  "clean" : false,
                  "failing" : true,
                  "totalFindings" : 1,
                  "byTool" : {
                    "pmd" : 1
                  },
                  "byRule" : {
                    "pmd:GodClass" : 1
                  },
                  "byDimension" : {
                    "maintainability" : 1
                  },
                  "findings" : [ {
                    "ruleId" : "pmd:GodClass",
                    "tool" : "pmd",
                    "dimension" : "maintainability",
                    "severity" : "low",
                    "file" : "Big.java",
                    "line" : 7,
                    "message" : "Too big"
                  } ]
                }""");
    }

    @Test
    void taskJsonShapeIsStable() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 7, "Too big")), 1);

        Path output = new AgentTaskExporter().write(result, tempDir, AgentTaskExporter.Format.JSON);

        assertThat(Files.readString(output)).isEqualTo(
                """
                        [ {
                          "id" : "HH-001",
                          "title" : "Fix pmd:GodClass",
                          "ruleId" : "pmd:GodClass",
                          "dimension" : "maintainability",
                          "severity" : "low",
                          "count" : 1,
                          "verificationCommand" : "habit-hooks --all",
                          "acceptanceCriteria" : [ "Resolve all current findings for pmd:GodClass.", "Keep the change focused and behavior-preserving unless the finding exposes a real bug.", "Re-run habit-hooks --all and confirm the rule no longer appears." ],
                          "locations" : [ "Big.java:7" ]
                        } ]""");
    }

    @Test
    void sarifMetadataUsesCanonicalRepositoryUrl() {
        Map<String, Object> sarif = SarifReportRenderer.render(fixedReport());

        assertThat(sarif.toString()).contains("https://github.com/patbaumgartner/habit-hooks");
        assertThat(sarif.toString()).doesNotContain("habbit-hooks");
    }

    private static QualityReport fixedReport() {
        ReportFinding finding = new ReportFinding("pmd:GodClass", "pmd", "maintainability", "low", "Big.java", 7,
                "Too big");
        return new QualityReport("2026-06-21T00:00:00Z", 1, false, true, 1, Map.of("pmd", 1L),
                Map.of("pmd:GodClass", 1L), Map.of("maintainability", 1L), List.of(finding));
    }

}
