package io.github.patbaumgartner.habithooks.cli;

import io.github.patbaumgartner.habithooks.analyzer.AnalysisOrchestrator;
import io.github.patbaumgartner.habithooks.analyzer.CheckstyleAnalyzer;
import io.github.patbaumgartner.habithooks.analyzer.PmdAnalyzer;
import io.github.patbaumgartner.habithooks.baseline.BaselineManager;
import io.github.patbaumgartner.habithooks.coaching.CoachingEngine;
import io.github.patbaumgartner.habithooks.coaching.CoachingRenderer;
import io.github.patbaumgartner.habithooks.coaching.PromptLoader;
import io.github.patbaumgartner.habithooks.config.ConfigLoader;
import io.github.patbaumgartner.habithooks.config.HabitHooksConfig;
import io.github.patbaumgartner.habithooks.model.AnalysisResult;
import io.github.patbaumgartner.habithooks.model.CoachingGroup;
import io.github.patbaumgartner.habithooks.model.Violation;
import io.github.patbaumgartner.habithooks.scope.FileScope;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

/**
 * Root CLI command for habit-hooks.
 *
 * <p>
 * Running without sub-commands performs a full analysis and prints coaching
 * output.
 */
@Command(name = "habit-hooks", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class, description = "Automated quality checks that nudge Java AI agents toward better habits.", subcommands = {
        InitCommand.class,
        BaselineCommand.class
})
public final class HabitHooksCommand implements Callable<Integer> {

    @Option(names = { "--config" }, description = "Path to the habit-hooks config file", scope = ScopeType.INHERIT)
    private String configPath;

    @Option(names = { "--all" }, description = "Analyze all Java files (overrides scope.onlyChangedFiles)")
    private boolean all;

    @Option(names = { "--last" }, description = "Analyze files changed in the last <n> commits", paramLabel = "<n>")
    private Integer last;

    @Option(names = {
            "--branch" }, description = "Analyze files changed vs branch (default: scope.branchBase)", paramLabel = "[name]", arity = "0..1", fallbackValue = "")
    private String branch;

    @Option(names = {
            "--since" }, description = "Analyze files changed since the given commit hash", paramLabel = "<hash>")
    private String since;

    @Override
    public Integer call() {
        Path workingDir = workingDir();
        HabitHooksConfig config = ConfigLoader.load(configPath, workingDir);
        List<Path> files = resolveScope(workingDir, config);

        if (files.isEmpty()) {
            System.out.println("No Java files to analyze.");
            return 0;
        }

        List<Violation> violations = runAnalysis(workingDir);
        BaselineManager baseline = new BaselineManager(workingDir);
        List<Violation> filtered = baseline.filter(violations);

        AnalysisResult result = new AnalysisResult(filtered, files.size());
        PromptLoader promptLoader = new PromptLoader(workingDir.resolve(config.getPromptsDir()));
        CoachingEngine engine = new CoachingEngine(promptLoader);
        List<CoachingGroup> groups = engine.coach(result);
        new CoachingRenderer(System.out).render(groups);

        return filtered.isEmpty() ? 0 : 1;
    }

    /**
     * Returns the current working directory as the project root.
     *
     * @return the working directory path
     */
    Path workingDir() {
        return Path.of(System.getProperty("user.dir", "."));
    }

    /**
     * Runs the configured analyzers against the resolved file scope and returns
     * raw (un-filtered) violations.
     *
     * @param workingDir the project root
     * @return raw violations before baseline filtering
     */
    List<Violation> runAnalysis(Path workingDir) {
        HabitHooksConfig config = ConfigLoader.load(configPath, workingDir);
        List<Path> files = resolveScope(workingDir, config);
        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(List.of(
                new CheckstyleAnalyzer(),
                new PmdAnalyzer()));
        return orchestrator.analyze(files, workingDir).violations();
    }

    private List<Path> resolveScope(Path workingDir, HabitHooksConfig config) {
        FileScope scope = new FileScope(workingDir);
        boolean excludeTests = config.getScope().isExcludeTests();
        List<Path> files;
        if (all) {
            files = scope.allFiles(excludeTests);
        } else if (last != null) {
            files = scope.changedInLastN(last);
        } else if (branch != null) {
            String base = branch.isBlank() ? config.getScope().getBranchBase() : branch;
            files = scope.changedSinceBranch(base);
        } else if (config.getScope().isOnlyChangedFiles()) {
            files = scope.changedSinceBranch(config.getScope().getBranchBase());
        } else {
            files = scope.allFiles(excludeTests);
        }
        if (excludeTests) {
            files = files.stream()
                    .filter(p -> !p.toString().contains("/src/test/")
                            && !p.toString().contains("\\src\\test\\"))
                    .toList();
        }
        return files;
    }
}
