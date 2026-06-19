package io.github.patbaumgartner.habithooks.analyzer;

import io.github.patbaumgartner.habithooks.model.AnalysisResult;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates multiple {@link Analyzer} implementations and merges their
 * results
 * into a single {@link AnalysisResult}.
 */
public final class AnalysisOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final List<Analyzer> analyzers;

    /**
     * Creates an orchestrator with the given set of analyzers.
     *
     * @param analyzers the analyzers to run
     */
    public AnalysisOrchestrator(List<Analyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    /**
     * Runs all available analyzers against the given files and returns the merged
     * result.
     *
     * @param files      the Java source files to analyze
     * @param workingDir the project root used to resolve config file paths
     * @return the merged analysis result
     */
    public AnalysisResult analyze(List<Path> files, Path workingDir) {
        List<Violation> allViolations = new ArrayList<>();
        for (Analyzer analyzer : analyzers) {
            allViolations.addAll(runSingle(analyzer, files, workingDir));
        }
        return new AnalysisResult(allViolations, files.size());
    }

    private List<Violation> runSingle(Analyzer analyzer, List<Path> files, Path workingDir) {
        if (!analyzer.isAvailable(workingDir)) {
            LOGGER.debug("Analyzer '{}' is not available in {}; skipping.",
                    analyzer.toolPrefix(), workingDir);
            return List.of();
        }
        LOGGER.debug("Running analyzer '{}' against {} files.",
                analyzer.toolPrefix(), files.size());
        return analyzer.analyze(files, workingDir);
    }
}
