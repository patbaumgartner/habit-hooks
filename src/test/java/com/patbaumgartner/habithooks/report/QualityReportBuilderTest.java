package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

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

    @Test
    void reportCollectionsAreImmutable() {
        QualityReport report = new QualityReport("2026-06-21T00:00:00Z", 1, false, true, 0, Map.of("pmd", 1L),
                Map.of("pmd:GodClass", 1L), Map.of("maintainability", 1L), List.of());

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> report.byTool().clear());
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> report.findings().clear());
    }

}
