package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Orchestrates multiple {@link Analyzer} implementations and merges their results into a
 * single {@link AnalysisResult}.
 */
public final class AnalysisOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final List<Analyzer> analyzers;

    /**
     * Creates an orchestrator with the given set of analyzers.
     * @param analyzers the analyzers to run
     */
    public AnalysisOrchestrator(List<Analyzer> analyzers) {
        this.analyzers = List.copyOf(analyzers);
    }

    /**
     * Runs all available analyzers against the given files and returns the merged result.
     * @param files the Java source files to analyze
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
        String analyzerName = analyzer.toolPrefix();
        if (!analyzer.isAvailable(workingDir)) {
            return unavailableAnalyzer(analyzerName, workingDir);
        }
        if (files.isEmpty() && analyzer.requiresFiles()) {
            return noFilesMatched(analyzerName);
        }
        LOGGER.atDebug()
            .addArgument(() -> analyzerName)
            .addArgument(() -> files.size())
            .log("Running analyzer '{}' against {} files.");
        return analyzer.analyze(files, workingDir);
    }

    private static List<Violation> unavailableAnalyzer(String analyzerName, Path workingDir) {
        LOGGER.atWarn()
            .addArgument(() -> analyzerName)
            .addArgument(workingDir)
            .log("Analyzer '{}' is not ready in {}; skipping. Run 'habit-hooks doctor' for setup details.");
        return List.of();
    }

    private static List<Violation> noFilesMatched(String analyzerName) {
        LOGGER.atDebug().addArgument(() -> analyzerName).log("Skipping analyzer '{}' because no files matched scope.");
        return List.of();
    }

}
