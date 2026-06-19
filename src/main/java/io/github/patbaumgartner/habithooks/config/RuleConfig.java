package io.github.patbaumgartner.habithooks.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Per-rule configuration override. All fields are optional; unset fields fall
 * back to the tool's own defaults.
 */
public class RuleConfig {

    /** When {@code true}, suppresses the coaching prompt for this rule. */
    @JsonProperty("disabled")
    private boolean disabled;

    /**
     * Ant-style glob patterns for files to include in this rule's scope.
     * When empty, all files in the analysis scope are included.
     */
    @JsonProperty("include")
    private String[] include = new String[0];

    /**
     * Ant-style glob patterns for files to exclude from this rule's scope.
     */
    @JsonProperty("exclude")
    private String[] exclude = new String[0];

    /**
     * Override severity: {@code "error"} exits non-zero, {@code "warning"} is
     * informational only.
     */
    @JsonProperty("severity")
    private String severity;

    /** Returns whether coaching for this rule is disabled. */
    public boolean isDisabled() {
        return disabled;
    }

    /** Sets whether coaching for this rule is disabled. */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /** Returns include glob patterns. */
    public String[] getInclude() {
        return include.clone();
    }

    /** Sets include glob patterns. */
    public void setInclude(String[] include) {
        this.include = include.clone();
    }

    /** Returns exclude glob patterns. */
    public String[] getExclude() {
        return exclude.clone();
    }

    /** Sets exclude glob patterns. */
    public void setExclude(String[] exclude) {
        this.exclude = exclude.clone();
    }

    /** Returns the configured severity override, or {@code null} if unset. */
    public String getSeverity() {
        return severity;
    }

    /** Sets the severity override. */
    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
