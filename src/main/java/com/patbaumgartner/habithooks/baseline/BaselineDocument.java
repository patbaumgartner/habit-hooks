package com.patbaumgartner.habithooks.baseline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The serialized form of the {@code .habit-hooks-baseline.json} file.
 *
 * <p>
 * The file is committed to version control and records known violations that should be
 * suppressed (snoozed) until the file is touched.
 */
public class BaselineDocument {

    /** Baseline version for forward-compatibility. */
    @JsonProperty("version")
    private int version = 1;

    /**
     * Map of workspace-relative file path → baseline entry. Only files with snoozed
     * violations appear here.
     */
    @JsonProperty("entries")
    private Map<String, BaselineEntry> entries = new HashMap<>();

    /** Returns the baseline format version. */
    public int getVersion() {
        return version;
    }

    /** Sets the baseline format version. */
    public void setVersion(int version) {
        this.version = version;
    }

    /** Returns all baseline entries. */
    public Map<String, BaselineEntry> getEntries() {
        return Map.copyOf(entries);
    }

    /** Sets all baseline entries. */
    public void setEntries(Map<String, BaselineEntry> entries) {
        this.entries = entries == null ? new HashMap<>() : new HashMap<>(entries);
    }

    Map<String, BaselineEntry> mutableEntries() {
        return entries;
    }

    /**
     * A single file's snoozed violation state.
     */
    public static class BaselineEntry {

        /**
         * Git commit hash of the last commit touching this file when the baseline was
         * taken.
         */
        @JsonProperty("commitHash")
        private String commitHash;

        /** Rule IDs of violations snoozed for this file. */
        @JsonProperty("ruleIds")
        private List<String> ruleIds = List.of();

        /** Returns the commit hash. */
        public String getCommitHash() {
            return commitHash;
        }

        /** Sets the commit hash. */
        public void setCommitHash(String commitHash) {
            this.commitHash = commitHash;
        }

        /** Returns the snoozed rule IDs. */
        public List<String> getRuleIds() {
            return ruleIds;
        }

        /** Sets the snoozed rule IDs. */
        public void setRuleIds(List<String> ruleIds) {
            this.ruleIds = List.copyOf(ruleIds);
        }

    }

}
