package io.github.patbaumgartner.habithooks.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Scoping rules: controls which files are included in an analysis run.
 */
public class ScopeConfig {

    /** When {@code true}, only files changed since the branch base are analyzed. */
    @JsonProperty("onlyChangedFiles")
    private boolean onlyChangedFiles = true;

    /**
     * The git branch used as the base when computing the changed-file set.
     * Defaults to {@code "main"}.
     */
    @JsonProperty("branchBase")
    private String branchBase = "main";

    /** Returns whether only changed files should be analyzed. */
    public boolean isOnlyChangedFiles() {
        return onlyChangedFiles;
    }

    /** Sets whether only changed files should be analyzed. */
    public void setOnlyChangedFiles(boolean onlyChangedFiles) {
        this.onlyChangedFiles = onlyChangedFiles;
    }

    /** Returns the git branch used as the diff base. */
    public String getBranchBase() {
        return branchBase;
    }

    /** Sets the git branch used as the diff base. */
    public void setBranchBase(String branchBase) {
        this.branchBase = branchBase;
    }

    /**
     * When {@code true}, files under {@code src/test/} are excluded from analysis.
     * Defaults to {@code true} — tests have different conventions.
     */
    @JsonProperty("excludeTests")
    private boolean excludeTests = true;

    /** Returns whether test sources should be excluded from analysis. */
    public boolean isExcludeTests() {
        return excludeTests;
    }

    /** Sets whether test sources should be excluded from analysis. */
    public void setExcludeTests(boolean excludeTests) {
        this.excludeTests = excludeTests;
    }
}
