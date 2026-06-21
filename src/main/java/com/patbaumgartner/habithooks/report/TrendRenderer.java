package com.patbaumgartner.habithooks.report;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/** Renders local trend information for human-readable reports. */
final class TrendRenderer {

    private TrendRenderer() {
    }

    static String markdown(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        StringBuilder output = new StringBuilder("## Trend\n\n");
        if (previous.isEmpty()) {
            output.append("No previous local report snapshot.\n\n");
            return output.toString();
        }
        appendMarkdownSummary(output, report, previous.get());
        appendMarkdownDimensions(output, report, previous.get());
        return output.toString();
    }

    static String html(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        if (previous.isEmpty()) {
            return "<section><h2>Trend</h2><p>No previous local report snapshot.</p></section>";
        }
        StringBuilder output = new StringBuilder("<section><h2>Trend</h2>");
        TrendStore.Snapshot snapshot = previous.get();
        output.append("<p>Findings: ")
            .append(escape(formatDelta(report.totalFindings() - snapshot.totalFindings())))
            .append(" since ")
            .append(escape(snapshot.generatedAt()))
            .append(".</p><ul>");
        dimensions(report, snapshot).forEach(dimension -> output.append("<li>")
            .append(escape(dimension))
            .append(": ")
            .append(escape(formatDelta(delta(dimension, report.byDimension(), snapshot.byDimension()))))
            .append("</li>"));
        return output.append("</ul></section>").toString();
    }

    private static void appendMarkdownSummary(StringBuilder output, QualityReport report,
            TrendStore.Snapshot snapshot) {
        output.append("- Previous findings: ")
            .append(snapshot.totalFindings())
            .append(" (generated ")
            .append(snapshot.generatedAt())
            .append(")\n");
        output.append("- Current findings: ").append(report.totalFindings()).append('\n');
        output.append("- Delta: ")
            .append(formatDelta(report.totalFindings() - snapshot.totalFindings()))
            .append("\n\n");
    }

    private static void appendMarkdownDimensions(StringBuilder output, QualityReport report,
            TrendStore.Snapshot snapshot) {
        output.append("### By Dimension\n\n");
        dimensions(report, snapshot).forEach(dimension -> output.append("- ")
            .append(dimension)
            .append(": ")
            .append(formatDelta(delta(dimension, report.byDimension(), snapshot.byDimension())))
            .append('\n'));
        output.append('\n');
    }

    private static Set<String> dimensions(QualityReport report, TrendStore.Snapshot snapshot) {
        Set<String> dimensions = new TreeSet<>(snapshot.byDimension().keySet());
        dimensions.addAll(report.byDimension().keySet());
        return dimensions;
    }

    private static long delta(String key, Map<String, Long> current, Map<String, Long> previous) {
        return current.getOrDefault(key, 0L) - previous.getOrDefault(key, 0L);
    }

    private static String formatDelta(long delta) {
        return delta > 0 ? "+" + delta : Long.toString(delta);
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
