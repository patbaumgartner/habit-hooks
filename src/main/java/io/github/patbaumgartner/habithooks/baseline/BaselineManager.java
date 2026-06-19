package io.github.patbaumgartner.habithooks.baseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the {@code .habit-hooks-baseline.json} file.
 *
 * <p>
 * The baseline suppresses snoozed violations but only while the file has not
 * been modified since the baseline was taken. Touching the file (staging,
 * committing,
 * or modifying) invalidates its baseline entry.
 */
public final class BaselineManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineManager.class);
    private static final String BASELINE_FILE = ".habit-hooks-baseline.json";

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private final Path workingDir;
    private final GitBridge git;

    /**
     * Creates a baseline manager for the given project root.
     *
     * @param workingDir the project root directory
     */
    public BaselineManager(Path workingDir) {
        this(workingDir, new GitBridge(workingDir));
    }

    BaselineManager(Path workingDir, GitBridge git) {
        this.workingDir = workingDir;
        this.git = git;
    }

    /**
     * Filters the provided violations by removing those suppressed by the baseline.
     *
     * @param violations the raw violations from the analyzers
     * @return violations not suppressed by the current baseline
     */
    public List<Violation> filter(List<Violation> violations) {
        Optional<BaselineDocument> doc = load();
        if (doc.isEmpty()) {
            return violations;
        }
        return violations.stream()
                .filter(v -> !isSnoozed(v, doc.get()))
                .toList();
    }

    /**
     * Adds all current violations to the baseline (snooze mode).
     *
     * @param violations the violations to snooze
     */
    public void snooze(List<Violation> violations) {
        BaselineDocument doc = load().orElseGet(BaselineDocument::new);
        Map<String, List<Violation>> byFile = groupByFile(violations);
        byFile.forEach((file, fileViolations) -> addEntry(doc, file, fileViolations));
        save(doc);
        LOGGER.info("Baseline updated: {} file(s) snoozed.", byFile.size());
    }

    /**
     * Removes baseline entries for files that no longer exist.
     */
    public void prune() {
        Optional<BaselineDocument> doc = load();
        if (doc.isEmpty()) {
            return;
        }
        BaselineDocument baseline = doc.get();
        Set<String> toRemove = baseline.getEntries().keySet().stream()
                .filter(file -> !Files.exists(workingDir.resolve(file)))
                .collect(Collectors.toSet());
        toRemove.forEach(baseline.getEntries()::remove);
        save(baseline);
        LOGGER.info("Baseline pruned: {} stale entries removed.", toRemove.size());
    }

    /**
     * Returns a human-readable summary of the current baseline contents.
     *
     * @return the summary text
     */
    public String status() {
        Optional<BaselineDocument> doc = load();
        if (doc.isEmpty()) {
            return "No baseline file found.";
        }
        BaselineDocument baseline = doc.get();
        int fileCount = baseline.getEntries().size();
        long ruleCount = baseline.getEntries().values().stream()
                .mapToLong(e -> e.getRuleIds().size())
                .sum();
        return String.format("Baseline: %d file(s), %d snoozed violation(s).",
                fileCount, ruleCount);
    }

    private boolean isSnoozed(Violation violation, BaselineDocument doc) {
        BaselineDocument.BaselineEntry entry = doc.getEntries().get(violation.file());
        if (entry == null) {
            return false;
        }
        if (!entry.getRuleIds().contains(violation.ruleId())) {
            return false;
        }
        String currentHash = git.lastCommitHash(violation.file()).orElse(null);
        return entry.getCommitHash() != null
                && entry.getCommitHash().equals(currentHash)
                && !git.isDirty(violation.file());
    }

    private void addEntry(
            BaselineDocument doc,
            String file,
            List<Violation> violations) {
        BaselineDocument.BaselineEntry entry = new BaselineDocument.BaselineEntry();
        entry.setCommitHash(git.lastCommitHash(file).orElse(null));
        entry.setRuleIds(violations.stream().map(Violation::ruleId).distinct().toList());
        doc.getEntries().put(file, entry);
    }

    private Optional<BaselineDocument> load() {
        Path path = workingDir.resolve(BASELINE_FILE);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try {
            BaselineDocument doc = JSON_MAPPER.readValue(path.toFile(), BaselineDocument.class);
            return Optional.of(doc);
        } catch (IOException e) {
            LOGGER.warn("Failed to read baseline file: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void save(BaselineDocument doc) {
        Path path = workingDir.resolve(BASELINE_FILE);
        try {
            JSON_MAPPER.writeValue(path.toFile(), doc);
        } catch (IOException e) {
            LOGGER.error("Failed to write baseline file: {}", e.getMessage());
        }
    }

    private static Map<String, List<Violation>> groupByFile(List<Violation> violations) {
        return violations.stream().collect(Collectors.groupingBy(Violation::file));
    }
}
