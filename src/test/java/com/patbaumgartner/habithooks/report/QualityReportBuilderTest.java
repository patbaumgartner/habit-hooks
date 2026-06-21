package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QualityReportBuilderTest {

    @Test
    void countMapsUseDeterministicKeyOrder() {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "Big.java", 1, "big"),
                new Violation("owasp:CveHigh", "pom.xml", -1, "cve"),
                new Violation("jacoco:LineCoverage", "target/site/jacoco/jacoco.xml", 1, "coverage")), 2);

        QualityReport report = new QualityReportBuilder().build(result, true);

        assertThat(report.byTool().keySet()).containsExactly("jacoco", "owasp", "pmd");
        assertThat(report.byDimension().keySet()).containsExactly("maintainability", "supply-chain", "test-signal");
    }

}