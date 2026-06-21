package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QualityReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesMarkdownReport() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("owasp:CveHigh", "pom.xml", -1, "CVE")), 3);
        QualityReport report = new QualityReportBuilder().build(result, true);

        Path output = new QualityReportWriter().write(report, tempDir, "markdown");

        assertThat(output).hasFileName("report.md");
        assertThat(Files.readString(output)).contains("habit-hooks local quality report", "owasp:CveHigh");
    }

    @Test
    void writesJsonHtmlAndSarifReports() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 7, "Too big")), 1);
        QualityReport report = new QualityReportBuilder().build(result, true);

        Path json = new QualityReportWriter().write(report, tempDir, "json");
        Path html = new QualityReportWriter().write(report, tempDir, "html");
        Path sarif = new QualityReportWriter().write(report, tempDir, "sarif");

        assertThat(Files.readString(json)).contains("pmd:GodClass");
        assertThat(Files.readString(html)).contains("habit-hooks local quality report");
        assertThat(Files.readString(sarif)).contains("\"version\"", "Big.java");
    }

    @Test
    void writesTrendDetailsWhenPreviousSnapshotExists() throws Exception {
        QualityReport current = new QualityReportBuilder()
            .build(new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 7, "Too big")), 1), true);
        TrendStore.Snapshot previous = new TrendStore.Snapshot("2026-06-20T00:00:00Z", 3, java.util.Map.of());

        Path output = new QualityReportWriter().write(current, tempDir, ReportFormat.MARKDOWN,
                java.util.Optional.of(previous));

        assertThat(Files.readString(output)).contains("## Trend", "Previous findings: 3", "Delta: -2");
    }

    @Test
    void rejectsUnknownReportFormat() {
        assertThatThrownBy(() -> ReportFormat.parse("xml")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported report format");
    }

}
