package com.patbaumgartner.habithooks.coaching;

import java.util.Map;

/**
 * Maps rule IDs to their short human-readable titles shown in coaching output.
 *
 * <p>
 * If a rule has no entry here, the rule ID itself is used as the title.
 */
public final class RuleTitles {

    /**
     * Known rule titles keyed by rule ID (e.g. {@code "checkstyle:MethodLength"}).
     */
    public static final Map<String, String> TITLES = Map.ofEntries(
            // Checkstyle
            Map.entry("checkstyle:MethodLength", "Oversized Method"),
            Map.entry("checkstyle:ParameterNumber", "Too Many Parameters"),
            Map.entry("checkstyle:CyclomaticComplexity", "High Cyclomatic Complexity"),
            Map.entry("checkstyle:JavaNCSS", "High Non-Commenting Source Lines"),
            Map.entry("checkstyle:VisibilityModifier", "Weak Encapsulation"),
            Map.entry("checkstyle:MagicNumber", "Magic Number"),
            Map.entry("checkstyle:EmptyLineSeparator", "Missing Separation"),
            Map.entry("checkstyle:FileTabCharacter", "Tab Character"),
            Map.entry("checkstyle:NestedIfDepth", "Deeply Nested Conditions"),
            Map.entry("checkstyle:NestedTryDepth", "Deeply Nested Try Blocks"),
            Map.entry("checkstyle:BooleanExpressionComplexity", "Complex Boolean Expression"),
            // PMD — size / complexity
            Map.entry("pmd:NcssCount", "Oversized Method or Class"),
            Map.entry("pmd:ExcessiveParameterList", "Too Many Parameters"),
            Map.entry("pmd:CyclomaticComplexity", "High Cyclomatic Complexity"), Map.entry("pmd:GodClass", "God Class"),
            Map.entry("pmd:TooManyFields", "Too Many Fields"), Map.entry("pmd:TooManyMethods", "Too Many Methods"),
            Map.entry("pmd:CollapsibleIfStatements", "Collapsible If Statements"),
            Map.entry("pmd:SimplifiedTernary", "Simplifiable Ternary"),
            Map.entry("pmd:SingularField", "Singular Field"),
            // PMD — correctness
            Map.entry("pmd:UseEqualsToCompareStrings", "String Compared with =="),
            Map.entry("pmd:OverrideBothEqualsAndHashcode", "Equals Without hashCode"),
            Map.entry("pmd:EmptyCatchBlock", "Empty Catch Block"),
            Map.entry("pmd:PreserveStackTrace", "Lost Stack Trace"),
            Map.entry("pmd:LiteralsFirstInComparisons", "Null-Unsafe String Comparison"),
            // PMD — design / encapsulation
            Map.entry("pmd:ReturnEmptyCollectionRatherThanNull", "Null Instead of Empty Collection"),
            Map.entry("pmd:UseCollectionIsEmpty", "Use isEmpty()"),
            Map.entry("pmd:LooseCoupling", "Concrete Type in API"),
            Map.entry("pmd:AvoidReassigningParameters", "Reassigned Parameter"),
            Map.entry("pmd:ArrayIsStoredDirectly", "Array Stored Directly"),
            Map.entry("pmd:MethodReturnsInternalArray", "Internal Array Exposed"),
            // PMD — unused
            Map.entry("pmd:UnusedPrivateField", "Unused Private Field"),
            Map.entry("pmd:UnusedLocalVariable", "Unused Local Variable"),
            // PMD — duplication
            Map.entry("pmd:CopyPaste", "Duplicated Code"),
            // Maven-backed project checks
            Map.entry("spotbugs:goal-failed", "SpotBugs Goal Failed"),
            Map.entry("spotbugs:lifecycle-blocked", "SpotBugs Lifecycle Blocked"),
            Map.entry("spotbugs:report-missing", "SpotBugs Report Missing"),
            Map.entry("spotbugs:report-unreadable", "SpotBugs Report Unreadable"),
            Map.entry("jacoco:LineCoverage", "Coverage Gap"), Map.entry("jacoco:goal-failed", "JaCoCo Goal Failed"),
            Map.entry("jacoco:lifecycle-blocked", "JaCoCo Lifecycle Blocked"),
            Map.entry("jacoco:report-missing", "JaCoCo Report Missing"),
            Map.entry("jacoco:report-unreadable", "JaCoCo Report Unreadable"),
            Map.entry("cyclonedx:InvalidBom", "Invalid SBOM"),
            Map.entry("cyclonedx:MissingComponents", "SBOM Missing Components"),
            Map.entry("cyclonedx:goal-failed", "CycloneDX Goal Failed"),
            Map.entry("cyclonedx:lifecycle-blocked", "CycloneDX Lifecycle Blocked"),
            Map.entry("cyclonedx:report-missing", "CycloneDX Report Missing"),
            Map.entry("cyclonedx:report-unreadable", "CycloneDX Report Unreadable"),
            Map.entry("pitest:SURVIVED", "Surviving Mutation"),
            Map.entry("pitest:NO_COVERAGE", "Mutation Without Coverage"),
            Map.entry("pitest:goal-failed", "PIT Goal Failed"),
            Map.entry("pitest:lifecycle-blocked", "PIT Lifecycle Blocked"),
            Map.entry("pitest:report-missing", "PIT Report Missing"),
            Map.entry("pitest:report-unreadable", "PIT Report Unreadable"),
            Map.entry("spring-javaformat:Formatting", "Formatting Drift"),
            Map.entry("spring-javaformat:goal-failed", "Spring Java Format Failed"),
            Map.entry("spring-javaformat:lifecycle-blocked", "Spring Java Format Lifecycle Blocked"),
            Map.entry("spring-javaformat:report-missing", "Spring Java Format Output Missing"),
            Map.entry("spring-javaformat:report-unreadable", "Spring Java Format Output Unreadable"),
            Map.entry("errorprone:goal-failed", "Error Prone Compile Failed"),
            Map.entry("errorprone:lifecycle-blocked", "Error Prone Lifecycle Blocked"),
            Map.entry("errorprone:report-missing", "Error Prone Output Missing"),
            Map.entry("errorprone:report-unreadable", "Error Prone Output Unreadable"),
            Map.entry("owasp:CveCritical", "Critical CVE"), Map.entry("owasp:CveHigh", "High CVE"),
            Map.entry("owasp:CveMedium", "Medium CVE"), Map.entry("owasp:CveLow", "Low CVE"),
            Map.entry("owasp:SuppressedVulnerability", "Suppressed Vulnerability"),
            Map.entry("owasp:goal-failed", "OWASP Dependency Check Failed"),
            Map.entry("owasp:lifecycle-blocked", "OWASP Dependency Check Lifecycle Blocked"),
            Map.entry("owasp:report-missing", "OWASP Dependency Check Report Missing"),
            Map.entry("owasp:report-unreadable", "OWASP Dependency Check Report Unreadable"),
            Map.entry("jspecify:DependencyMissing", "JSpecify Dependency Missing"),
            Map.entry("jspecify:NotAdopted", "JSpecify Not Adopted"));

    private RuleTitles() {
        // utility class
    }

    /**
     * Returns the human-readable title for the given rule ID, falling back to the rule ID
     * itself when no title is registered.
     * @param ruleId the rule ID (e.g. {@code "checkstyle:MethodLength"})
     * @return the title, never {@code null}
     */
    public static String titleFor(String ruleId) {
        return TITLES.getOrDefault(ruleId, ruleId);
    }

}
