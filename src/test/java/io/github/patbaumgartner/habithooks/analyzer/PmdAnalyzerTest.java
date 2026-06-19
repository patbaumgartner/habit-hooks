package io.github.patbaumgartner.habithooks.analyzer;

import io.github.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class PmdAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void isNotAvailableWhenRulesetMissing() {
        PmdAnalyzer analyzer = new PmdAnalyzer("pmd-ruleset.xml");
        assertThat(analyzer.isAvailable(tempDir)).isFalse();
    }

    @Test
    void isAvailableWhenRulesetPresent() throws IOException {
        Path ruleset = tempDir.resolve("pmd-ruleset.xml");
        Files.writeString(ruleset, minimalPmdRuleset());
        PmdAnalyzer analyzer = new PmdAnalyzer("pmd-ruleset.xml");
        assertThat(analyzer.isAvailable(tempDir)).isTrue();
    }

    @Test
    void returnsEmptyListForEmptyFileSet() throws IOException {
        Path ruleset = tempDir.resolve("pmd-ruleset.xml");
        Files.writeString(ruleset, minimalPmdRuleset());
        PmdAnalyzer analyzer = new PmdAnalyzer("pmd-ruleset.xml");
        List<Violation> violations = analyzer.analyze(List.of(), tempDir);
        assertThat(violations).isEmpty();
    }

    @Test
    void toolPrefixIsPmd() {
        assertThat(new PmdAnalyzer().toolPrefix()).isEqualTo("pmd");
    }

    @Test
    void detectsEmptyCatchBlock() throws IOException {
        Path ruleset = tempDir.resolve("pmd-ruleset.xml");
        Files.writeString(ruleset, emptyCatchRuleset());
        Path javaFile = tempDir.resolve("EmptyCatch.java");
        Files.writeString(javaFile, """
                public class EmptyCatch {
                    public void method() {
                        try {
                            int x = Integer.parseInt("bad");
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                """);
        PmdAnalyzer analyzer = new PmdAnalyzer("pmd-ruleset.xml");
        List<Violation> violations = analyzer.analyze(List.of(javaFile), tempDir);
        assertThat(violations).isNotEmpty();
        assertThat(violations.get(0).ruleId()).isEqualTo("pmd:EmptyCatchBlock");
    }

    private static String minimalPmdRuleset() {
        return """
                <?xml version="1.0"?>
                <ruleset name="minimal"
                    xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 \
                https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
                    <description>minimal</description>
                </ruleset>
                """;
    }

    private static String emptyCatchRuleset() {
        return """
                <?xml version="1.0"?>
                <ruleset name="test"
                    xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 \
                https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
                    <description>test</description>
                    <rule ref="category/java/errorprone.xml/EmptyCatchBlock"/>
                </ruleset>
                """;
    }
}
