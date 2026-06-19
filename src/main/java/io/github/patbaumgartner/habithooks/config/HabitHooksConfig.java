package io.github.patbaumgartner.habithooks.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration object for habit-hooks, deserialized from
 * {@code .habit-hooks.yaml} in the project root.
 *
 * <p>
 * All fields are optional and fall back to sensible defaults so that a
 * minimal config (or no config at all) still produces useful output.
 */
public class HabitHooksConfig {

    /**
     * Directory containing custom coaching prompt files.
     * Defaults to {@code "./prompts"}.
     */
    @JsonProperty("prompts")
    private String promptsDir = "./prompts";

    /**
     * Per-rule overrides, keyed by rule ID (e.g.
     * {@code "checkstyle:MethodLength"}).
     */
    @JsonProperty("rules")
    private Map<String, RuleConfig> rules = new HashMap<>();

    /** Scope configuration. */
    @JsonProperty("scope")
    private ScopeConfig scope = new ScopeConfig();

    /**
     * Analyzer configurations, keyed by analyzer name ({@code "checkstyle"},
     * {@code "pmd"}).
     */
    @JsonProperty("analyzers")
    private Map<String, AnalyzerConfig> analyzers = new HashMap<>();

    /** Returns the prompts directory path. */
    public String getPromptsDir() {
        return promptsDir;
    }

    /** Sets the prompts directory path. */
    public void setPromptsDir(String promptsDir) {
        this.promptsDir = promptsDir;
    }

    /** Returns the per-rule overrides map. */
    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    /** Sets the per-rule overrides map. */
    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules;
    }

    /** Returns the scope configuration. */
    public ScopeConfig getScope() {
        return scope;
    }

    /** Sets the scope configuration. */
    public void setScope(ScopeConfig scope) {
        this.scope = scope;
    }

    /** Returns the analyzer configurations. */
    public Map<String, AnalyzerConfig> getAnalyzers() {
        return analyzers;
    }

    /** Sets the analyzer configurations. */
    public void setAnalyzers(Map<String, AnalyzerConfig> analyzers) {
        this.analyzers = analyzers;
    }
}
