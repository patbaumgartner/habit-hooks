package io.github.patbaumgartner.habithooks.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for a single static-analysis analyzer (Checkstyle or PMD).
 */
public class AnalyzerConfig {

    /** Whether this analyzer is active. Defaults to {@code true}. */
    @JsonProperty("enabled")
    private boolean enabled = true;

    /**
     * Path to the Checkstyle config file (Checkstyle only).
     * Defaults to {@code "checkstyle.xml"}.
     */
    @JsonProperty("configFile")
    private String configFile = "checkstyle.xml";

    /**
     * PMD rulesets to apply (PMD only). Defaults to a single
     * {@code "pmd-ruleset.xml"} entry.
     */
    @JsonProperty("rulesets")
    private String[] rulesets = {"pmd-ruleset.xml"};

    /** Returns whether this analyzer is enabled. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Sets whether this analyzer is enabled. */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /** Returns the Checkstyle config file path. */
    public String getConfigFile() {
        return configFile;
    }

    /** Sets the Checkstyle config file path. */
    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    /** Returns the PMD ruleset paths. */
    public String[] getRulesets() {
        return rulesets.clone();
    }

    /** Sets the PMD ruleset paths. */
    public void setRulesets(String[] rulesets) {
        this.rulesets = rulesets.clone();
    }
}
