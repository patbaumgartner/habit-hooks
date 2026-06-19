package io.github.patbaumgartner.habithooks.coaching;

import java.util.Map;

/**
 * Maps rule IDs to their short human-readable titles shown in coaching output.
 *
 * <p>If a rule has no entry here, the rule ID itself is used as the title.
 */
public final class RuleTitles {

    /**
     * Known rule titles keyed by rule ID (e.g. {@code "checkstyle:MethodLength"}).
     */
    public static final Map<String, String> TITLES = Map.ofEntries(
            // Checkstyle
            Map.entry("checkstyle:MethodLength",         "Oversized Method"),
            Map.entry("checkstyle:ParameterNumber",      "Too Many Parameters"),
            Map.entry("checkstyle:CyclomaticComplexity", "High Cyclomatic Complexity"),
            Map.entry("checkstyle:JavaNCSS",             "High Non-Commenting Source Lines"),
            Map.entry("checkstyle:VisibilityModifier",   "Weak Encapsulation"),
            Map.entry("checkstyle:MagicNumber",          "Magic Number"),
            Map.entry("checkstyle:NestedIfDepth",        "Deeply Nested Conditions"),
            Map.entry("checkstyle:NestedTryDepth",       "Deeply Nested Try Blocks"),
            Map.entry("checkstyle:BooleanExpressionComplexity", "Complex Boolean Expression"),
            // PMD
            Map.entry("pmd:NcssCount",                   "Oversized Method or Class"),
            Map.entry("pmd:ExcessiveParameterList",      "Too Many Parameters"),
            Map.entry("pmd:CyclomaticComplexity",        "High Cyclomatic Complexity"),
            Map.entry("pmd:GodClass",                    "God Class"),
            Map.entry("pmd:TooManyFields",               "Too Many Fields"),
            Map.entry("pmd:TooManyMethods",              "Too Many Methods"),
            Map.entry("pmd:UnusedPrivateField",          "Unused Private Field"),
            Map.entry("pmd:UnusedLocalVariable",         "Unused Local Variable"),
            Map.entry("pmd:CopyPaste",                   "Duplicated Code")
    );

    private RuleTitles() {
        // utility class
    }

    /**
     * Returns the human-readable title for the given rule ID, falling back to
     * the rule ID itself when no title is registered.
     *
     * @param ruleId the rule ID (e.g. {@code "checkstyle:MethodLength"})
     * @return the title, never {@code null}
     */
    public static String titleFor(String ruleId) {
        return TITLES.getOrDefault(ruleId, ruleId);
    }
}
