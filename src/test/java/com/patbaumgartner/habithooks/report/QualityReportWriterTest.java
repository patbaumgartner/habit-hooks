package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        assertThat(Files.readString(sarif)).contains("\"version\"", "Big.java", "\"rules\"",
                "\"id\" : \"pmd:GodClass\"");
    }

    @Test
    void writesToExactReportFileWhenOutputHasFormatExtension() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 7, "Too big")), 1);
        QualityReport report = new QualityReportBuilder().build(result, true);
        Path requestedOutput = tempDir.resolve("agent/petclinic-report.md");

        Path output = new QualityReportWriter().write(report, requestedOutput, "markdown");

        assertThat(output).isEqualTo(requestedOutput);
        assertThat(Files.exists(requestedOutput)).isTrue();
        assertThat(Files.isDirectory(requestedOutput)).isFalse();
    }

    @Test
    void writesTrendDetailsWhenPreviousSnapshotExists() throws Exception {
        QualityReport current = new QualityReportBuilder()
            .build(new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 7, "Too big")), 1), true);
        TrendStore.Snapshot previous = new TrendStore.Snapshot("2026-06-21T00:00:00Z", 3,
                Map.of("maintainability", 3L, "supply-chain", 1L));

        Path output = new QualityReportWriter().write(current, tempDir, ReportFormat.MARKDOWN, Optional.of(previous));

        assertThat(Files.readString(output)).contains("## Trend", "Previous findings: 3", "Delta: -2",
                "maintainability: -2", "supply-chain: -1");
    }

    @Test
    void writesHtmlTrendDetailsWhenPreviousSnapshotExists() throws Exception {
        QualityReport current = new QualityReportBuilder()
            .build(new AnalysisResult(List.of(new Violation("owasp:CveHigh", "pom.xml", 1, "cve")), 1), true);
        TrendStore.Snapshot previous = new TrendStore.Snapshot("2026-06-21T00:00:00Z", 0, Map.of());

        Path output = new QualityReportWriter().write(current, tempDir, ReportFormat.HTML, Optional.of(previous));

        assertThat(Files.readString(output)).contains("<h2>Trend</h2>", "supply-chain: +1");
    }

    @Test
    void rejectsUnknownReportFormat() {
        assertThatThrownBy(() -> ReportFormat.parse("xml")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported report format");
    }

}
