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

        assertThat(metadata).contains("com/puppycrawl/tools/checkstyle/.*\\\\.dtd", "category/java/.*\\\\.xml",
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

    private static List<Class<?>> jacksonTypes() {
        return List.of(HabitHooksConfig.class, ScopeConfig.class, AnalyzerConfig.class, RuleConfig.class,
                BaselineDocument.class, BaselineDocument.BaselineEntry.class, AnalysisResult.class, Violation.class,
                QualityReport.class, ReportFinding.class, TrendStore.Snapshot.class, AgentTask.class);
    }

}