package com.patbaumgartner.habithooks.report;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class TrendStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void recordsSnapshotAndReturnsPreviousRun() throws Exception {
        TrendStore store = new TrendStore();
        QualityReport first = report("pmd:GodClass", "Big.java", "big");
        QualityReport second = report("owasp:CveHigh", "pom.xml", "cve");

        assertThat(store.record(tempDir, first)).isEmpty();

        TrendStore.Snapshot previous = store.record(tempDir, second).orElseThrow();

        assertThat(previous.totalFindings()).isEqualTo(1);
        assertThat(previous.byDimension()).containsEntry("maintainability", 1L);
        assertThat(Files.readString(tempDir.resolve("latest.json"))).contains("supply-chain");
    }

    @Test
    void storesDimensionsInStableOrder() {
        TrendStore.Snapshot snapshot = new TrendStore.Snapshot("2026-06-21T00:00:00Z", 2,
                Map.of("supply-chain", 1L, "maintainability", 1L));

        assertThat(snapshot.byDimension().keySet()).containsExactly("maintainability", "supply-chain");
    }

    private static QualityReport report(String ruleId, String file, String message) {
        AnalysisResult result = new AnalysisResult(List.of(new Violation(ruleId, file, 1, message)), 1);
        return new QualityReportBuilder().build(result, true);
    }

}
