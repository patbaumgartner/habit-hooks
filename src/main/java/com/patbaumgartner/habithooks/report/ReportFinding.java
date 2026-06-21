package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.Violation;
import java.util.Comparator;
import java.util.Map;

/** A report-friendly view of a normalized analyzer violation. */
public record ReportFinding(String ruleId, String tool, String dimension, String severity, String file, int line,
        String message) {

    private static final Map<String, String> DIMENSIONS_BY_TOOL = Map.of("owasp", "supply-chain", "cyclonedx",
            "supply-chain", "jacoco", "test-signal", "pitest", "test-signal", "taikai", "architecture",
            "spring-javaformat", "formatting", "spotbugs", "correctness", "errorprone", "correctness");

    private static final int CRITICAL_RANK = 0;

    private static final int HIGH_RANK = 1;

    private static final int MEDIUM_RANK = 2;

    private static final int LOW_RANK = 3;

    private static final int UNKNOWN_RANK = 4;

    /** Creates a report finding from a raw violation. */
    public static ReportFinding from(Violation violation) {
        String tool = toolOf(violation.ruleId());
        return new ReportFinding(violation.ruleId(), tool, dimensionOf(tool, violation.ruleId()),
                severityOf(violation.ruleId()), violation.file(), violation.line(), violation.message());
    }

    /** Orders findings by remediation priority, then by stable location details. */
    public static Comparator<ReportFinding> priorityOrder() {
        return Comparator.comparingInt(ReportFinding::severityRank)
            .thenComparing(ReportFinding::dimension)
            .thenComparing(ReportFinding::ruleId)
            .thenComparing(ReportFinding::file)
            .thenComparingInt(ReportFinding::line);
    }

    private int severityRank() {
        return switch (severity) {
            case "critical" -> CRITICAL_RANK;
            case "high" -> HIGH_RANK;
            case "medium" -> MEDIUM_RANK;
            case "low" -> LOW_RANK;
            default -> UNKNOWN_RANK;
        };
    }

    private static String toolOf(String ruleId) {
        int separator = ruleId.indexOf(':');
        return separator < 0 ? ruleId : ruleId.substring(0, separator);
    }

    private static String dimensionOf(String tool, String ruleId) {
        String dimension = DIMENSIONS_BY_TOOL.get(tool);
        if (dimension != null) {
            return dimension;
        }
        if (ruleId.contains("Null")) {
            return "correctness";
        }
        return "maintainability";
    }

    private static String severityOf(String ruleId) {
        if (ruleId.contains("Critical")) {
            return "critical";
        }
        if (ruleId.contains("High") || ruleId.endsWith("goal-failed")) {
            return "high";
        }
        if (ruleId.contains("Medium") || ruleId.endsWith("report-missing")) {
            return "medium";
        }
        return "low";
    }

}
