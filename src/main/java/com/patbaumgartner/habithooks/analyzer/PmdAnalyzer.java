package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.reporting.Report;
import net.sourceforge.pmd.reporting.RuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the PMD Java API to report violations as {@link Violation} records.
 *
 * <p>
 * Uses one or more ruleset XML files located relative to the working directory.
 * Violations carry rule IDs prefixed with {@code "pmd:"}.
 */
public final class PmdAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PmdAnalyzer.class);

    private static final String TOOL_PREFIX = "pmd";

    private static final String DEFAULT_RULESET = "pmd-ruleset.xml";

    private static final String JAVA_LANGUAGE = "java";

    private static final String JAVA_VERSION = "25";

    private final List<String> rulesets;

    /**
     * Creates an analyzer using the given PMD ruleset file names.
     * @param rulesets ruleset file names relative to the working directory
     */
    public PmdAnalyzer(String... rulesets) {
        this.rulesets = List.of(rulesets);
    }

    /** Creates an analyzer using the default {@code pmd-ruleset.xml}. */
    public PmdAnalyzer() {
        this(DEFAULT_RULESET);
    }

    @Override
    public String toolPrefix() {
        return TOOL_PREFIX;
    }

    @Override
    public boolean isAvailable(Path workingDir) {
        return rulesets.stream().anyMatch(r -> Files.isRegularFile(workingDir.resolve(r)));
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        if (files.isEmpty()) {
            return List.of();
        }
        List<String> resolvedRulesets = resolveRulesets(workingDir);
        if (resolvedRulesets.isEmpty()) {
            LOGGER.warn("No PMD ruleset files found in {}; skipping PMD analysis.", workingDir);
            return List.of();
        }
        try {
            return runPmd(files, resolvedRulesets, workingDir);
        }
        catch (RuntimeException | Error ex) {
            LOGGER.warn("PMD analysis aborted: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<String> resolveRulesets(Path workingDir) {
        return rulesets.stream()
            .map(workingDir::resolve)
            .filter(Files::isRegularFile)
            .map(Path::toAbsolutePath)
            .map(Path::toString)
            .toList();
    }

    private static List<Violation> runPmd(List<Path> files, List<String> resolvedRulesets, Path workingDir) {
        PMDConfiguration config = buildConfig(files, resolvedRulesets);
        List<Violation> violations = new ArrayList<>();

        try (PmdAnalysis analysis = PmdAnalysis.create(config)) {
            Report report = analysis.performAnalysisAndCollectReport();
            for (RuleViolation rv : report.getViolations()) {
                violations.add(toViolation(rv, workingDir));
            }
            if (!report.getProcessingErrors().isEmpty()) {
                report.getProcessingErrors().forEach(e -> LOGGER.warn("PMD processing error: {}", e.getMsg()));
            }
        }
        return List.copyOf(violations);
    }

    private static PMDConfiguration buildConfig(List<Path> files, List<String> resolvedRulesets) {
        PMDConfiguration config = new PMDConfiguration();
        config.setDefaultLanguageVersion(getJavaLanguageVersion());
        config.setInputPathList(files);
        resolvedRulesets.forEach(config::addRuleSet);
        config.setIgnoreIncrementalAnalysis(true);
        return config;
    }

    private static LanguageVersion getJavaLanguageVersion() {
        LanguageVersion version = LanguageRegistry.PMD.getLanguageById(JAVA_LANGUAGE).getVersion(JAVA_VERSION);
        if (version == null) {
            return LanguageRegistry.PMD.getLanguageById(JAVA_LANGUAGE).getDefaultVersion();
        }
        return version;
    }

    private static Violation toViolation(RuleViolation rv, Path workingDir) {
        String ruleId = TOOL_PREFIX + ":" + rv.getRule().getName();
        String file = relativize(rv.getFileId().getOriginalPath(), workingDir);
        return new Violation(ruleId, file, rv.getBeginLine(), rv.getDescription());
    }

    private static String relativize(String absolutePath, Path workingDir) {
        if (absolutePath == null) {
            return "";
        }
        try {
            return workingDir.toAbsolutePath().relativize(Path.of(absolutePath)).toString();
        }
        catch (IllegalArgumentException e) {
            return absolutePath;
        }
    }

}
