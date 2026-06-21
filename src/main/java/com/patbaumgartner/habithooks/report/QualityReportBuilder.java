package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Builds compact local quality reports from analyzer results. */
public final class QualityReportBuilder {

    /** Builds a report for the provided analysis result. */
    public QualityReport build(AnalysisResult result, boolean failing) {
        List<ReportFinding> findings = result.violations()
            .stream()
            .map(ReportFinding::from)
            .sorted(ReportFinding.priorityOrder())
            .toList();
        return new QualityReport(Instant.now().toString(), result.filesChecked(), result.isClean(), failing,
                findings.size(), count(findings, ReportFinding::tool), count(findings, ReportFinding::ruleId),
                count(findings, ReportFinding::dimension), findings);
    }

    private static Map<String, Long> count(List<ReportFinding> findings, Function<ReportFinding, String> classifier) {
        return findings.stream().collect(Collectors.groupingBy(classifier, TreeMap::new, Collectors.counting()));
    }

}
