package com.patbaumgartner.habithooks.report;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Local quality report that can be rendered as JSON, Markdown, HTML, or SARIF.
 */
public record QualityReport(String generatedAt, int filesChecked, boolean clean, boolean failing, int totalFindings,
        Map<String, Long> byTool, Map<String, Long> byRule, Map<String, Long> byDimension,
        List<ReportFinding> findings) {

    /** Returns a defensive-copy report instance. */
    public QualityReport {
        byTool = orderedCopy(byTool);
        byRule = orderedCopy(byRule);
        byDimension = orderedCopy(byDimension);
        findings = List.copyOf(findings);
    }

    private static Map<String, Long> orderedCopy(Map<String, Long> values) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

}
