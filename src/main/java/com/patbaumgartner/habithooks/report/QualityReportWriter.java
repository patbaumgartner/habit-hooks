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

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Writes the report and returns the generated artifact path. */
    public Path write(QualityReport report, Path outputPath, String format) throws IOException {
        return write(report, outputPath, ReportFormat.parse(format), Optional.empty());
    }

    /** Writes the report and returns the generated artifact path. */
    public Path write(QualityReport report, Path outputPath, ReportFormat format,
            Optional<TrendStore.Snapshot> previous) throws IOException {
        String defaultFileName = "report." + format.extension();
        Path output = isFileOutput(outputPath, defaultFileName) ? outputPath : outputPath.resolve(defaultFileName);
        Files.createDirectories(artifactDirectory(outputPath, format));
        switch (format) {
            case JSON -> MAPPER.writeValue(output.toFile(), report);
            case HTML -> Files.writeString(output, HtmlReportRenderer.render(report, previous), StandardCharsets.UTF_8);
            case SARIF -> MAPPER.writeValue(output.toFile(), SarifReportRenderer.render(report));
            case MARKDOWN -> Files.writeString(output, markdown(report, previous), StandardCharsets.UTF_8);
        }
        return output;
    }

    /**
     * Returns the directory where generated report sidecars, such as history, belong.
     */
    public Path artifactDirectory(Path outputPath, ReportFormat format) {
        if (!isFileOutput(outputPath, "report." + format.extension())) {
            return outputPath;
        }
        Path parent = outputPath.getParent();
        return parent == null ? Path.of(".") : parent;
    }

    private static boolean isFileOutput(Path outputPath, String defaultFileName) {
        Path fileName = outputPath.getFileName();
        String extension = defaultFileName.substring(defaultFileName.lastIndexOf('.'));
        return fileName != null && fileName.toString().endsWith(extension);
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

    private static String location(ReportFinding finding) {
        return finding.line() > 0 ? finding.file() + ":" + finding.line() : finding.file();
    }

}
