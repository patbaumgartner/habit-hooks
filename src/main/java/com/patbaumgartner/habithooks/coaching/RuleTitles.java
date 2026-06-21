package com.patbaumgartner.habithooks.coaching;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps rule IDs to their short human-readable titles shown in coaching output.
 *
 * <p>
 * If a rule has no entry here, the rule ID itself is used as the title.
 */
public final class RuleTitles {

    private static final List<RuleTitle> TITLE_DATA = List.of(
            new RuleTitle("checkstyle:MethodLength", "Oversized Method"),
            new RuleTitle("checkstyle:ParameterNumber", "Too Many Parameters"),
            new RuleTitle("checkstyle:CyclomaticComplexity", "High Cyclomatic Complexity"),
            new RuleTitle("checkstyle:JavaNCSS", "High Non-Commenting Source Lines"),
            new RuleTitle("checkstyle:VisibilityModifier", "Weak Encapsulation"),
            new RuleTitle("checkstyle:MagicNumber", "Magic Number"),
            new RuleTitle("checkstyle:EmptyLineSeparator", "Missing Separation"),
            new RuleTitle("checkstyle:FileTabCharacter", "Tab Character"),
            new RuleTitle("checkstyle:NestedIfDepth", "Deeply Nested Conditions"),
            new RuleTitle("checkstyle:NestedTryDepth", "Deeply Nested Try Blocks"),
            new RuleTitle("checkstyle:BooleanExpressionComplexity", "Complex Boolean Expression"),
            new RuleTitle("checkstyle:EqualsAvoidNull", "Literal Not First in equals()"),
            new RuleTitle("checkstyle:CovariantEquals", "Covariant equals() Without Override"),
            new RuleTitle("checkstyle:DefaultComesLast", "Default Case Not Last"),
            new RuleTitle("checkstyle:ModifiedControlVariable", "Modified Loop Control Variable"),
            new RuleTitle("checkstyle:HideUtilityClassConstructor", "Utility Class Needs Private Constructor"),
            new RuleTitle("checkstyle:UnnecessarySemicolonInEnumeration", "Redundant Semicolon in Enum"),
            new RuleTitle("checkstyle:UnnecessarySemicolonAfterTypeMemberDeclaration",
                    "Redundant Semicolon After Member"),

            new RuleTitle("pmd:NcssCount", "Oversized Method or Class"),
            new RuleTitle("pmd:ExcessiveParameterList", "Too Many Parameters"),
            new RuleTitle("pmd:CyclomaticComplexity", "High Cyclomatic Complexity"),
            new RuleTitle("pmd:GodClass", "God Class"), new RuleTitle("pmd:TooManyFields", "Too Many Fields"),
            new RuleTitle("pmd:TooManyMethods", "Too Many Methods"),
            new RuleTitle("pmd:CollapsibleIfStatements", "Collapsible If Statements"),
            new RuleTitle("pmd:SimplifiedTernary", "Simplifiable Ternary"),
            new RuleTitle("pmd:SingularField", "Singular Field"),

            new RuleTitle("pmd:UseEqualsToCompareStrings", "String Compared with =="),
            new RuleTitle("pmd:OverrideBothEqualsAndHashcode", "Equals Without hashCode"),
            new RuleTitle("pmd:EmptyCatchBlock", "Empty Catch Block"),
            new RuleTitle("pmd:PreserveStackTrace", "Lost Stack Trace"),
            new RuleTitle("pmd:LiteralsFirstInComparisons", "Null-Unsafe String Comparison"),

            new RuleTitle("pmd:ReturnEmptyCollectionRatherThanNull", "Null Instead of Empty Collection"),
            new RuleTitle("pmd:UseCollectionIsEmpty", "Use isEmpty()"),
            new RuleTitle("pmd:LooseCoupling", "Concrete Type in API"),
            new RuleTitle("pmd:AvoidReassigningParameters", "Reassigned Parameter"),
            new RuleTitle("pmd:ArrayIsStoredDirectly", "Array Stored Directly"),
            new RuleTitle("pmd:MethodReturnsInternalArray", "Internal Array Exposed"),
            new RuleTitle("pmd:CompareObjectsWithEquals", "Reference Comparison Instead of equals()"),
            new RuleTitle("pmd:EqualsNull", "equals() Used for Null Check"),
            new RuleTitle("pmd:MissingOverride", "Missing @Override"),
            new RuleTitle("pmd:PrimitiveWrapperInstantiation", "Primitive Wrapper Instantiated"),
            new RuleTitle("pmd:GuardLogStatement", "Unguarded Log Statement"),
            new RuleTitle("pmd:UselessParentheses", "Useless Parentheses"),
            new RuleTitle("pmd:UnnecessaryReturn", "Redundant Return"),
            new RuleTitle("pmd:UnnecessaryModifier", "Redundant Modifier"),
            new RuleTitle("pmd:UnusedPrivateField", "Unused Private Field"),
            new RuleTitle("pmd:UnusedLocalVariable", "Unused Local Variable"),
            new RuleTitle("pmd:UnusedPrivateMethod", "Unused Private Method"),
            new RuleTitle("pmd:UnusedFormalParameter", "Unused Parameter"),
            new RuleTitle("pmd:UnusedAssignment", "Unused Assignment"),
            new RuleTitle("pmd:CopyPaste", "Duplicated Code"),

            new RuleTitle("cpd:goal-failed", "CPD Goal Failed"),
            new RuleTitle("cpd:lifecycle-blocked", "CPD Lifecycle Blocked"),
            new RuleTitle("cpd:report-missing", "CPD Report Missing"),
            new RuleTitle("cpd:report-unreadable", "CPD Report Unreadable"),

            new RuleTitle("spotbugs:goal-failed", "SpotBugs Goal Failed"),
            new RuleTitle("spotbugs:lifecycle-blocked", "SpotBugs Lifecycle Blocked"),
            new RuleTitle("spotbugs:report-missing", "SpotBugs Report Missing"),
            new RuleTitle("spotbugs:report-unreadable", "SpotBugs Report Unreadable"),
            new RuleTitle("jacoco:LineCoverage", "Coverage Gap"),
            new RuleTitle("jacoco:goal-failed", "JaCoCo Goal Failed"),
            new RuleTitle("jacoco:lifecycle-blocked", "JaCoCo Lifecycle Blocked"),
            new RuleTitle("jacoco:report-missing", "JaCoCo Report Missing"),
            new RuleTitle("jacoco:report-unreadable", "JaCoCo Report Unreadable"),
            new RuleTitle("cyclonedx:InvalidBom", "Invalid SBOM"),
            new RuleTitle("cyclonedx:MissingComponents", "SBOM Missing Components"),
            new RuleTitle("cyclonedx:goal-failed", "CycloneDX Goal Failed"),
            new RuleTitle("cyclonedx:lifecycle-blocked", "CycloneDX Lifecycle Blocked"),
            new RuleTitle("cyclonedx:report-missing", "CycloneDX Report Missing"),
            new RuleTitle("cyclonedx:report-unreadable", "CycloneDX Report Unreadable"),
            new RuleTitle("pitest:SURVIVED", "Surviving Mutation"),
            new RuleTitle("pitest:NO_COVERAGE", "Mutation Without Coverage"),
            new RuleTitle("pitest:goal-failed", "PIT Goal Failed"),
            new RuleTitle("pitest:lifecycle-blocked", "PIT Lifecycle Blocked"),
            new RuleTitle("pitest:report-missing", "PIT Report Missing"),
            new RuleTitle("pitest:report-unreadable", "PIT Report Unreadable"),
            new RuleTitle("spring-javaformat:Formatting", "Formatting Drift"),
            new RuleTitle("spring-javaformat:goal-failed", "Spring Java Format Failed"),
            new RuleTitle("spring-javaformat:lifecycle-blocked", "Spring Java Format Lifecycle Blocked"),
            new RuleTitle("spring-javaformat:report-missing", "Spring Java Format Output Missing"),
            new RuleTitle("spring-javaformat:report-unreadable", "Spring Java Format Output Unreadable"),
            new RuleTitle("errorprone:goal-failed", "Error Prone Compile Failed"),
            new RuleTitle("errorprone:lifecycle-blocked", "Error Prone Lifecycle Blocked"),
            new RuleTitle("errorprone:report-missing", "Error Prone Output Missing"),
            new RuleTitle("errorprone:report-unreadable", "Error Prone Output Unreadable"),
            new RuleTitle("owasp:CveCritical", "Critical CVE"), new RuleTitle("owasp:CveHigh", "High CVE"),
            new RuleTitle("owasp:CveMedium", "Medium CVE"), new RuleTitle("owasp:CveLow", "Low CVE"),
            new RuleTitle("owasp:SuppressedVulnerability", "Suppressed Vulnerability"),
            new RuleTitle("owasp:goal-failed", "OWASP Dependency Check Failed"),
            new RuleTitle("owasp:lifecycle-blocked", "OWASP Dependency Check Lifecycle Blocked"),
            new RuleTitle("owasp:report-missing", "OWASP Dependency Check Report Missing"),
            new RuleTitle("owasp:report-unreadable", "OWASP Dependency Check Report Unreadable"),
            new RuleTitle("jspecify:DependencyMissing", "JSpecify Dependency Missing"),
            new RuleTitle("jspecify:NotAdopted", "JSpecify Not Adopted"));

    /**
     * Known rule titles keyed by rule ID (e.g. {@code "checkstyle:MethodLength"}).
     */
    public static final Map<String, String> TITLES = buildTitles();

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

    private static Map<String, String> buildTitles() {
        Map<String, String> titles = new LinkedHashMap<>();
        for (RuleTitle title : TITLE_DATA) {
            titles.put(title.ruleId(), title.title());
        }
        return Map.copyOf(titles);
    }

    private record RuleTitle(String ruleId, String title) {
    }

}
