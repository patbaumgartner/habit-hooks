package io.github.patbaumgartner.habithooks.model;

import java.util.List;

/**
 * The aggregated result of running all configured analyzers against a set of
 * files.
 *
 * @param violations   all violations found (may be empty)
 * @param filesChecked number of source files actually analyzed
 */
public record AnalysisResult(
        List<Violation> violations,
        int filesChecked) {

    /**
     * Constructs an AnalysisResult with a defensive copy of the violations list.
     */
    public AnalysisResult {
        violations = List.copyOf(violations);
        if (filesChecked < 0) {
            throw new IllegalArgumentException("filesChecked must be >= 0");
        }
    }

    /** Returns {@code true} when no violations were found. */
    public boolean isClean() {
        return violations.isEmpty();
    }
}
