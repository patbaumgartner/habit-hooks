package com.patbaumgartner.habithooks.config;

import com.patbaumgartner.habithooks.baseline.BaselineDocument;
import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import com.patbaumgartner.habithooks.report.QualityReport;
import com.patbaumgartner.habithooks.report.ReportFinding;
import com.patbaumgartner.habithooks.report.TrendStore;
import com.patbaumgartner.habithooks.tasks.AgentTask;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NativeImageMetadataTest {

    private static final Path REFLECT_CONFIG = Path
        .of("src/main/resources/META-INF/native-image/com.patbaumgartner/habit-hooks/reflect-config.json");

    private static final Path RESOURCE_CONFIG = Path
        .of("src/main/resources/META-INF/native-image/com.patbaumgartner/habit-hooks/resource-config.json");

    @Test
    void registersJacksonTypesForNativeReflection() throws Exception {
        String metadata = Files.readString(REFLECT_CONFIG);

        for (Class<?> jacksonType : jacksonTypes()) {
            assertThat(metadata).contains("\"name\": \"" + jacksonType.getName() + "\"");
        }
        assertThat(metadata).contains("\"allDeclaredConstructors\": true", "\"allDeclaredFields\": true",
                "\"allDeclaredMethods\": true");
    }

    @Test
    void registersBundledAnalyzerRulesetsAsNativeResources() throws Exception {
        String metadata = Files.readString(RESOURCE_CONFIG);

        assertThat(metadata).contains("com/puppycrawl/tools/checkstyle/.*\\\\.dtd",
                "com/puppycrawl/tools/checkstyle/.*messages.*\\\\.properties", "category/java/.*\\\\.xml",
                "rulesets/.*\\\\.xml");
    }

    @Test
    void registersPmdRulesForNativeReflection() throws Exception {
        String metadata = Files.readString(REFLECT_CONFIG);

        assertThat(metadata).contains("net.sourceforge.pmd.lang.rule.xpath.XPathRule",
                "net.sourceforge.pmd.lang.java.rule.design.NcssCountRule",
                "net.sourceforge.pmd.lang.java.rule.design.ExcessiveParameterListRule",
                "net.sourceforge.pmd.lang.java.rule.design.CyclomaticComplexityRule",
                "net.sourceforge.pmd.lang.java.rule.design.GodClassRule",
                "net.sourceforge.pmd.lang.java.rule.design.SingularFieldRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.UseCollectionIsEmptyRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedPrivateFieldRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.UnusedLocalVariableRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.AvoidReassigningParametersRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.LooseCouplingRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.LiteralsFirstInComparisonsRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.ArrayIsStoredDirectlyRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.MethodReturnsInternalArrayRule",
                "net.sourceforge.pmd.lang.java.rule.bestpractices.PreserveStackTraceRule",
                "net.sourceforge.pmd.lang.java.rule.errorprone.OverrideBothEqualsAndHashcodeRule",
                "net.sourceforge.pmd.lang.java.rule.errorprone.ProperCloneImplementationRule",
                "net.sourceforge.pmd.lang.java.rule.performance.UseStringBufferForStringAppendsRule");
    }

    @Test
    void registersBeanUtilsArrayConvertersForCheckstyleNativeReflection() throws Exception {
        String metadata = Files.readString(REFLECT_CONFIG);

        assertThat(metadata).contains("[Z", "[B", "[C", "[D", "[F", "[I", "[J", "[S", "[Ljava.lang.Boolean;",
                "[Ljava.lang.Byte;", "[Ljava.lang.Character;", "[Ljava.lang.Double;", "[Ljava.lang.Float;",
                "[Ljava.lang.Integer;", "[Ljava.lang.Long;", "[Ljava.lang.Short;", "[Ljava.lang.String;",
                "[Ljava.lang.Class;", "[Ljava.math.BigDecimal;", "[Ljava.math.BigInteger;", "[Ljava.util.Date;",
                "[Ljava.util.Calendar;", "[Ljava.io.File;", "[Ljava.sql.Date;", "[Ljava.sql.Time;",
                "[Ljava.sql.Timestamp;", "[Ljava.net.URL;", "[Ljava.util.regex.Pattern;",
                "[Lcom.puppycrawl.tools.checkstyle.checks.naming.AccessModifierOption;");
    }

    @Test
    void registersConfiguredCheckstyleModulesForNativeReflection() throws Exception {
        String metadata = Files.readString(REFLECT_CONFIG);

        assertThat(metadata).contains("com.puppycrawl.tools.checkstyle.api.TokenTypes", "\"allPublicFields\": true",
                "com.puppycrawl.tools.checkstyle.Checker", "com.puppycrawl.tools.checkstyle.TreeWalker",
                "com.puppycrawl.tools.checkstyle.checks.whitespace.FileTabCharacterCheck",
                "com.puppycrawl.tools.checkstyle.checks.NewlineAtEndOfFileCheck",
                "com.puppycrawl.tools.checkstyle.checks.sizes.FileLengthCheck",
                "com.puppycrawl.tools.checkstyle.checks.sizes.MethodLengthCheck",
                "com.puppycrawl.tools.checkstyle.checks.sizes.ParameterNumberCheck",
                "com.puppycrawl.tools.checkstyle.checks.metrics.CyclomaticComplexityCheck",
                "com.puppycrawl.tools.checkstyle.checks.metrics.JavaNCSSCheck",
                "com.puppycrawl.tools.checkstyle.checks.metrics.BooleanExpressionComplexityCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.TypeNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.MethodNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.ParameterNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.LocalVariableNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.MemberNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.StaticVariableNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.ConstantNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.PackageNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.naming.ClassTypeParameterNameCheck",
                "com.puppycrawl.tools.checkstyle.checks.design.VisibilityModifierCheck",
                "com.puppycrawl.tools.checkstyle.checks.design.FinalClassCheck",
                "com.puppycrawl.tools.checkstyle.checks.design.InnerTypeLastCheck",
                "com.puppycrawl.tools.checkstyle.checks.design.OneTopLevelClassCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.MagicNumberCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.InnerAssignmentCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanExpressionCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.SimplifyBooleanReturnCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.StringLiteralEqualityCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.NestedIfDepthCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.NestedTryDepthCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.MultipleVariableDeclarationsCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.OneStatementPerLineCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.FallThroughCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.UnnecessaryParenthesesCheck",
                "com.puppycrawl.tools.checkstyle.checks.coding.NoFinalizerCheck",
                "com.puppycrawl.tools.checkstyle.checks.imports.AvoidStarImportCheck",
                "com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck",
                "com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck",
                "com.puppycrawl.tools.checkstyle.checks.imports.IllegalImportCheck",
                "com.puppycrawl.tools.checkstyle.checks.blocks.EmptyBlockCheck",
                "com.puppycrawl.tools.checkstyle.checks.blocks.NeedBracesCheck",
                "com.puppycrawl.tools.checkstyle.checks.blocks.LeftCurlyCheck",
                "com.puppycrawl.tools.checkstyle.checks.TodoCommentCheck",
                "com.puppycrawl.tools.checkstyle.checks.UpperEllCheck",
                "com.puppycrawl.tools.checkstyle.checks.ArrayTypeStyleCheck");
    }

    private static List<Class<?>> jacksonTypes() {
        return List.of(HabitHooksConfig.class, ScopeConfig.class, AnalyzerConfig.class, RuleConfig.class,
                BaselineDocument.class, BaselineDocument.BaselineEntry.class, AnalysisResult.class, Violation.class,
                QualityReport.class, ReportFinding.class, TrendStore.Snapshot.class, AgentTask.class);
    }

}