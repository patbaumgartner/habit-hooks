package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Native-image-safe PMD analyzer backed by Maven's PMD report goal. */
public class MavenPmdAnalyzer extends MavenGoalAnalyzer {

    private static final String TOOL_PREFIX = "pmd";

    private static final String DEFAULT_REPORT = "target/pmd.xml";

    private static final String MAVEN_GOAL = "pmd:pmd -Dpmd.rulesets=";

    private final List<String> rulesets;

    /**
     * Creates a Maven-backed PMD analyzer using rulesets relative to the project root.
     * @param rulesets PMD ruleset paths
     */
    public MavenPmdAnalyzer(String... rulesets) {
        super(TOOL_PREFIX, MAVEN_GOAL + String.join(",", rulesets), DEFAULT_REPORT, ReportParsers.pmdXml());
        this.rulesets = List.of(rulesets);
    }

    @Override
    public boolean requiresFiles() {
        return true;
    }

    @Override
    public boolean isAvailable(Path workingDir) {
        return hasRuleset(workingDir) && super.isAvailable(workingDir);
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        List<Violation> violations = super.analyze(files, workingDir);
        Set<String> scopedFiles = scopedFiles(files, workingDir);
        return violations.stream().filter(violation -> inScope(violation, scopedFiles)).toList();
    }

    private boolean hasRuleset(Path workingDir) {
        return rulesets.stream().anyMatch(ruleset -> Files.isRegularFile(workingDir.resolve(ruleset)));
    }

    private static Set<String> scopedFiles(List<Path> files, Path workingDir) {
        return files.stream()
            .map(Path::toAbsolutePath)
            .map(Path::normalize)
            .map(path -> ReportSupport.relativize(path, workingDir))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean inScope(Violation violation, Set<String> scopedFiles) {
        if (!violation.file().endsWith(".java")) {
            return true;
        }
        return scopedFiles.contains(violation.file());
    }

}
