package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.analyzer.AnalysisOrchestrator;
import com.patbaumgartner.habithooks.analyzer.Analyzer;
import com.patbaumgartner.habithooks.analyzer.CheckstyleAnalyzer;
import com.patbaumgartner.habithooks.analyzer.PmdAnalyzer;
import com.patbaumgartner.habithooks.analyzer.TaikaiAnalyzer;
import com.patbaumgartner.habithooks.baseline.BaselineManager;
import com.patbaumgartner.habithooks.coaching.CoachingEngine;
import com.patbaumgartner.habithooks.coaching.CoachingRenderer;
import com.patbaumgartner.habithooks.coaching.PromptLoader;
import com.patbaumgartner.habithooks.config.AnalyzerConfig;
import com.patbaumgartner.habithooks.config.ConfigLoader;
import com.patbaumgartner.habithooks.config.HabitHooksConfig;
import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.CoachingGroup;
import com.patbaumgartner.habithooks.model.Violation;
import com.patbaumgartner.habithooks.scope.FileScope;
import com.patbaumgartner.habithooks.update.SelfUpdater;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

/**
 * Root CLI command for habit-hooks.
 *
 * <p>
 * Running without sub-commands performs a full analysis and prints coaching output.
 */
@Command(name = "habit-hooks", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        description = "Automated quality checks that nudge Java AI agents toward better habits.",
        subcommands = { InitCommand.class, BaselineCommand.class })
public final class HabitHooksCommand implements Callable<Integer> {

    @Option(names = { "--config" }, description = "Path to the habit-hooks config file", scope = ScopeType.INHERIT)
    private String configPath;

    @Option(names = { "--all" }, description = "Analyze all Java files (overrides scope.onlyChangedFiles)")
    private boolean all;

    @Option(names = { "--last" }, description = "Analyze files changed in the last <n> commits", paramLabel = "<n>")
    private Integer last;

    @Option(names = { "--branch" }, description = "Analyze files changed vs branch (default: scope.branchBase)",
            paramLabel = "[name]", arity = "0..1", fallbackValue = "")
    private String branch;

    @Option(names = { "--since" }, description = "Analyze files changed since the given commit hash",
            paramLabel = "<hash>")
    private String since;

    @Option(names = { "--update" }, description = "Download and install the latest habit-hooks release")
    private boolean update;

    @Override
    public Integer call() {
        if (this.update) {
            return new SelfUpdater(VersionProvider.currentVersion(), System.out).run();
        }
        return runChecks();
    }

    private Integer runChecks() {
        Path workingDir = workingDir();
        HabitHooksConfig config = ConfigLoader.load(configPath, workingDir);
        List<Path> files = resolveScope(workingDir, config);
        List<Analyzer> analyzers = createAnalyzers(config);

        if (files.isEmpty()) {
            System.out.println("No Java files to analyze.");
            return 0;
        }
        if (analyzers.isEmpty()) {
            System.out.println("No analyzers enabled in configuration.");
            return 0;
        }

        List<Violation> violations = runAnalysis(workingDir, files, analyzers);
        List<Violation> filtered = new BaselineManager(workingDir).filter(violations);
        RuleFilter ruleFilter = new RuleFilter(config.getRules());
        List<Violation> configured = ruleFilter.apply(filtered);

        AnalysisResult result = new AnalysisResult(configured, files.size());
        PromptLoader promptLoader = new PromptLoader(workingDir.resolve(config.getPromptsDir()));
        List<CoachingGroup> groups = new CoachingEngine(promptLoader).coach(result);
        new CoachingRenderer(System.out).render(groups);

        return ruleFilter.hasFailures(configured) ? 1 : 0;
    }

    /**
     * Returns the current working directory as the project root.
     * @return the working directory path
     */
    Path workingDir() {
        return Path.of(System.getProperty("user.dir", "."));
    }

    /**
     * Runs the configured analyzers against the resolved file scope and returns raw
     * (un-filtered) violations.
     * @param workingDir the project root
     * @return raw violations before baseline filtering
     */
    List<Violation> runAnalysis(Path workingDir) {
        HabitHooksConfig config = ConfigLoader.load(configPath, workingDir);
        List<Path> files = resolveScope(workingDir, config);
        return runAnalysis(workingDir, files, createAnalyzers(config));
    }

    private List<Violation> runAnalysis(Path workingDir, List<Path> files, List<Analyzer> analyzers) {
        if (analyzers.isEmpty()) {
            return List.of();
        }
        return new AnalysisOrchestrator(analyzers).analyze(files, workingDir).violations();
    }

    private static List<Analyzer> createAnalyzers(HabitHooksConfig config) {
        List<Analyzer> analyzers = new ArrayList<>();
        Map<String, AnalyzerConfig> analyzerConfig = config.getAnalyzers();

        AnalyzerConfig checkstyle = analyzerConfig.getOrDefault("checkstyle", new AnalyzerConfig());
        if (checkstyle.isEnabled()) {
            analyzers.add(new CheckstyleAnalyzer(checkstyle.getConfigFile()));
        }

        AnalyzerConfig pmd = analyzerConfig.getOrDefault("pmd", new AnalyzerConfig());
        if (pmd.isEnabled()) {
            analyzers.add(new PmdAnalyzer(pmd.getRulesets()));
        }

        if (analyzerConfig.containsKey("taikai")) {
            AnalyzerConfig taikai = analyzerConfig.get("taikai");
            if (taikai.isEnabled()) {
                analyzers.add(new TaikaiAnalyzer(taikai.getTestClass()));
            }
        }

        return List.copyOf(analyzers);
    }

    private List<Path> resolveScope(Path workingDir, HabitHooksConfig config) {
        FileScope scope = new FileScope(workingDir);
        boolean excludeTests = config.getScope().isExcludeTests();
        return filterTests(selectFiles(scope, config, excludeTests), excludeTests);
    }

    private List<Path> selectFiles(FileScope scope, HabitHooksConfig config, boolean excludeTests) {
        if (all) {
            return scope.allFiles(excludeTests);
        }
        if (last != null) {
            return scope.changedInLastN(last);
        }
        if (branch != null) {
            String base = branch.isBlank() ? config.getScope().getBranchBase() : branch;
            return scope.changedSinceBranch(base);
        }
        if (since != null) {
            return scope.changedSinceCommit(since);
        }
        if (config.getScope().isOnlyChangedFiles()) {
            return scope.changedSinceBranch(config.getScope().getBranchBase());
        }
        return scope.allFiles(excludeTests);
    }

    private List<Path> filterTests(List<Path> files, boolean excludeTests) {
        if (!excludeTests) {
            return files;
        }
        return files.stream()
            .filter(p -> !p.toString().contains("/src/test/") && !p.toString().contains("\\src\\test\\"))
            .toList();
    }

}
