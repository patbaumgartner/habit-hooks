package com.patbaumgartner.habithooks.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Stores tiny local quality snapshots for trend-aware reports. */
public final class TrendStore {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Records the current report and returns the previous snapshot when present.
     */
    public Optional<Snapshot> record(Path outputDir, QualityReport report) throws IOException {
        Files.createDirectories(outputDir);
        Path latest = outputDir.resolve("latest.json");
        Optional<Snapshot> previous = read(latest);
        Snapshot snapshot = Snapshot.from(report);
        MAPPER.writeValue(latest.toFile(), snapshot);
        return previous;
    }

    private Optional<Snapshot> read(Path latest) throws IOException {
        if (!Files.isRegularFile(latest)) {
            return Optional.empty();
        }
        return Optional.of(MAPPER.readValue(latest.toFile(), Snapshot.class));
    }

    /** A stable trend snapshot for the latest local run. */
    public record Snapshot(String generatedAt, int totalFindings, Map<String, Long> byDimension) {

        static Snapshot from(QualityReport report) {
            return new Snapshot(report.generatedAt(), report.totalFindings(), report.byDimension());
        }

        public Snapshot {
            byDimension = Collections.unmodifiableMap(new TreeMap<>(byDimension));
        }

    }

}
