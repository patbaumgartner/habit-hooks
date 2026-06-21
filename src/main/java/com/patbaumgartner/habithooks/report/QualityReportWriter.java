package com.patbaumgartner.habithooks.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Writes local quality reports in agent- and tool-friendly formats. */
public final class QualityReportWriter {

    private static final int MARKDOWN_FINDING_LIMIT = 50;

    private static final int HTML_FINDING_LIMIT = 100;

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Writes the report and returns the generated artifact path. */
    public Path write(QualityReport report, Path outputDir, String format) throws IOException {
        return write(report, outputDir, ReportFormat.parse(format), Optional.empty());
    }

    /** Writes the report and returns the generated artifact path. */
    public Path write(QualityReport report, Path outputDir, ReportFormat format, Optional<TrendStore.Snapshot> previous)
            throws IOException {
        Files.createDirectories(outputDir);
        Path output = outputDir.resolve("report." + format.extension());
        switch (format) {
            case JSON -> MAPPER.writeValue(output.toFile(), report);
            case HTML -> Files.writeString(output, html(report, previous), StandardCharsets.UTF_8);
            case SARIF -> MAPPER.writeValue(output.toFile(), SarifReportRenderer.render(report));
            case MARKDOWN -> Files.writeString(output, markdown(report, previous), StandardCharsets.UTF_8);
        }
        return output;
    }

    private static String markdown(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        StringBuilder output = new StringBuilder("# habit-hooks local quality report\n\n");
        appendSummary(output, report);
        output.append(TrendRenderer.markdown(report, previous));
        appendMap(output, "By dimension", report.byDimension());
        appendMap(output, "By tool", report.byTool());
        appendFindings(output, report.findings());
        return output.toString();
    }

    private static void appendSummary(StringBuilder output, QualityReport report) {
        output.append("- Generated: ").append(report.generatedAt()).append('\n');
        output.append("- Files checked: ").append(report.filesChecked()).append('\n');
        output.append("- Findings: ").append(report.totalFindings()).append('\n');
        output.append("- Gate: ").append(report.failing() ? "failing" : "passing").append("\n\n");
    }

    private static void appendMap(StringBuilder output, String heading, Map<String, Long> counts) {
        output.append("## ").append(heading).append("\n\n");
        if (counts.isEmpty()) {
            output.append("No findings.\n\n");
            return;
        }
        counts.forEach((key, value) -> output.append("- ").append(key).append(": ").append(value).append('\n'));
        output.append('\n');
    }

    private static void appendFindings(StringBuilder output, List<ReportFinding> findings) {
        output.append("## Agent task feed\n\n");
        findings.stream().limit(MARKDOWN_FINDING_LIMIT).forEach(finding -> appendFinding(output, finding));
        if (findings.size() > MARKDOWN_FINDING_LIMIT) {
            output.append("\n_Only the first 50 findings are shown._\n");
        }
    }

    private static void appendFinding(StringBuilder output, ReportFinding finding) {
        output.append("- [")
            .append(finding.severity())
            .append("] ")
            .append(finding.ruleId())
            .append(" at ")
            .append(location(finding))
            .append(" — ")
            .append(finding.message())
            .append('\n');
    }

    private static String html(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        return "<!doctype html><html><head><meta charset=\"utf-8\"><title>habit-hooks report</title>"
                + "<style>body{font-family:system-ui;margin:2rem;max-width:72rem}li{margin:.35rem 0}"
                + "code{background:#eee;padding:.1rem .25rem}</style></head><body>" + htmlBody(report, previous)
                + "</body></html>";
    }

    private static String htmlBody(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        StringBuilder body = new StringBuilder("<h1>habit-hooks local quality report</h1>");
        body.append("<p>Findings: ")
            .append(report.totalFindings())
            .append(". Gate: ")
            .append(report.failing() ? "failing" : "passing")
            .append(".</p>")
            .append(TrendRenderer.html(report, previous))
            .append("<ul>");
        report.findings()
            .stream()
            .limit(HTML_FINDING_LIMIT)
            .forEach(finding -> body.append("<li><code>")
                .append(escape(finding.ruleId()))
                .append("</code> ")
                .append(escape(location(finding)))
                .append(" - ")
                .append(escape(finding.message()))
                .append("</li>"));
        return body.append("</ul>").toString();
    }

    private static String location(ReportFinding finding) {
        return finding.line() > 0 ? finding.file() + ":" + finding.line() : finding.file();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
