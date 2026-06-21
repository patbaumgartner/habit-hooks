package com.patbaumgartner.habithooks.report;

import java.util.List;
import java.util.Map;

/** Renders a quality report as SARIF 2.1.0. */
final class SarifReportRenderer {

    private SarifReportRenderer() {
    }

    static Map<String, Object> render(QualityReport report) {
        List<Map<String, Object>> results = report.findings().stream().map(SarifReportRenderer::result).toList();
        Map<String, Object> driver = Map.of("name", "habit-hooks", "informationUri",
                "https://github.com/patbaumgartner/habbit-hooks");
        return Map.of("version", "2.1.0", "$schema", "https://json.schemastore.org/sarif-2.1.0.json", "runs",
                List.of(Map.of("tool", Map.of("driver", driver), "results", results)));
    }

    private static Map<String, Object> result(ReportFinding finding) {
        Map<String, Object> region = Map.of("startLine", Math.max(1, finding.line()));
        Map<String, Object> location = Map.of("physicalLocation",
                Map.of("artifactLocation", Map.of("uri", finding.file()), "region", region));
        return Map.of("ruleId", finding.ruleId(), "level", level(finding.severity()), "message",
                Map.of("text", finding.message()), "locations", List.of(location));
    }

    private static String level(String severity) {
        return switch (severity) {
            case "critical", "high" -> "error";
            case "medium" -> "warning";
            default -> "note";
        };
    }

}
