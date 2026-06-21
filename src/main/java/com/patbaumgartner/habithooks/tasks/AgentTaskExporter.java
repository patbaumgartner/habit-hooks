package com.patbaumgartner.habithooks.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.report.ReportFinding;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Exports analyzer findings as agent-sized work items. */
public final class AgentTaskExporter {

    private static final int LOCATION_LIMIT = 10;

    private static final String VERIFICATION_COMMAND = "habit-hooks --all";

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /** Writes agent tasks and returns the generated path. */
    public Path write(AnalysisResult result, Path outputDir, String format) throws IOException {
        return write(result, outputDir, Format.parse(format));
    }

    /** Writes agent tasks and returns the generated path. */
    public Path write(AnalysisResult result, Path outputDir, Format format) throws IOException {
        Files.createDirectories(outputDir);
        List<AgentTask> tasks = tasks(result);
        Path output = outputDir.resolve("tasks." + format.extension());
        if (format == Format.JSON) {
            MAPPER.writeValue(output.toFile(), tasks);
        }
        else {
            Files.writeString(output, markdown(tasks), StandardCharsets.UTF_8);
        }
        return output;
    }

    /** Builds agent-sized tasks grouped by normalized rule ID. */
    public List<AgentTask> tasks(AnalysisResult result) {
        AtomicInteger sequence = new AtomicInteger(1);
        return result.violations()
            .stream()
            .map(ReportFinding::from)
            .collect(Collectors.groupingBy(ReportFinding::ruleId))
            .entrySet()
            .stream()
            .sorted(MapEntryComparator.INSTANCE.thenComparing(Map.Entry::getKey))
            .map(entry -> toTask(sequence.getAndIncrement(), entry.getKey(), entry.getValue()))
            .toList();
    }

    private static AgentTask toTask(int sequence, String ruleId, List<ReportFinding> findings) {
        List<ReportFinding> sortedFindings = findings.stream().sorted(ReportFinding.priorityOrder()).toList();
        ReportFinding first = sortedFindings.getFirst();
        return new AgentTask("HH-" + String.format("%03d", sequence), title(first), ruleId, first.dimension(),
                first.severity(), sortedFindings.size(), VERIFICATION_COMMAND, acceptanceCriteria(first),
                locations(sortedFindings));
    }

    private static String title(ReportFinding finding) {
        return switch (finding.dimension()) {
            case "supply-chain" -> "Resolve " + finding.ruleId();
            case "test-signal" -> "Improve test signal for " + finding.ruleId();
            case "architecture" -> "Restore architecture rule " + finding.ruleId();
            default -> "Fix " + finding.ruleId();
        };
    }

    private static List<String> locations(List<ReportFinding> findings) {
        return findings.stream().map(AgentTaskExporter::location).distinct().limit(LOCATION_LIMIT).toList();
    }

    private static List<String> acceptanceCriteria(ReportFinding finding) {
        return List.of("Resolve all current findings for " + finding.ruleId() + ".",
                "Keep the change focused and behavior-preserving unless the finding exposes a real bug.",
                "Re-run " + VERIFICATION_COMMAND + " and confirm the rule no longer appears.");
    }

    private static String markdown(List<AgentTask> tasks) {
        StringBuilder output = new StringBuilder("# habit-hooks agent tasks\n\n");
        output.append("Work these in priority order. Keep each task focused, then re-run `")
            .append(VERIFICATION_COMMAND)
            .append("`.\n\n");
        if (tasks.isEmpty()) {
            output.append("No tasks generated.\n");
            return output.toString();
        }
        tasks.forEach(task -> appendTask(output, task));
        return output.toString();
    }

    private static void appendTask(StringBuilder output, AgentTask task) {
        output.append("## ").append(task.id()).append(' ').append(task.title()).append("\n\n");
        output.append("- Rule: `").append(task.ruleId()).append("`\n");
        output.append("- Dimension: ").append(task.dimension()).append("\n");
        output.append("- Severity: ").append(task.severity()).append("\n");
        output.append("- Findings: ").append(task.count()).append("\n");
        output.append("- Verification: `").append(task.verificationCommand()).append("`\n");
        output.append("- Acceptance criteria:\n");
        task.acceptanceCriteria().forEach(criterion -> output.append("  - ").append(criterion).append('\n'));
        task.locations().forEach(location -> output.append("- Location: ").append(location).append('\n'));
        output.append('\n');
    }

    private static String location(ReportFinding finding) {
        return finding.line() > 0 ? finding.file() + ":" + finding.line() : finding.file();
    }

    /** Supported task export formats. */
    public enum Format {

        /** Markdown task list, written as {@code tasks.md}. */
        MARKDOWN("md"),

        /** JSON task list, written as {@code tasks.json}. */
        JSON("json");

        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        String extension() {
            return extension;
        }

        /** Parses a user-supplied format name. */
        public static Format parse(String value) {
            String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "markdown", "md" -> MARKDOWN;
                case "json" -> JSON;
                default -> throw new IllegalArgumentException(
                        "Unsupported task format '" + value + "'. Use one of: markdown, md, json.");
            };
        }

    }

    private enum MapEntryComparator implements Comparator<Map.Entry<String, List<ReportFinding>>> {

        INSTANCE;

        @Override
        public int compare(Map.Entry<String, List<ReportFinding>> left, Map.Entry<String, List<ReportFinding>> right) {
            return ReportFinding.priorityOrder()
                .compare(priorityFinding(left.getValue()), priorityFinding(right.getValue()));
        }

        private static ReportFinding priorityFinding(List<ReportFinding> findings) {
            return findings.stream().min(ReportFinding.priorityOrder()).orElseThrow();
        }

    }

}
