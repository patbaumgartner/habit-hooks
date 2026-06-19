package io.github.patbaumgartner.habithooks.analyzer;

import io.github.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class CheckstyleAnalyzerTest {

    @TempDir
    Path tempDir;

    @Test
    void isNotAvailableWhenConfigFileMissing() {
        CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer("checkstyle.xml");
        assertThat(analyzer.isAvailable(tempDir)).isFalse();
    }

    @Test
    void isAvailableWhenConfigFilePresent() throws IOException {
        Path config = tempDir.resolve("checkstyle.xml");
        Files.writeString(config, minimalCheckstyleConfig());
        CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer("checkstyle.xml");
        assertThat(analyzer.isAvailable(tempDir)).isTrue();
    }

    @Test
    void returnsEmptyListForEmptyFileSet() throws IOException {
        Path config = tempDir.resolve("checkstyle.xml");
        Files.writeString(config, minimalCheckstyleConfig());
        CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer("checkstyle.xml");
        List<Violation> violations = analyzer.analyze(List.of(), tempDir);
        assertThat(violations).isEmpty();
    }

    @Test
    void toolPrefixIsCheckstyle() {
        assertThat(new CheckstyleAnalyzer().toolPrefix()).isEqualTo("checkstyle");
    }

    @Test
    void detectsMethodLengthViolation() throws IOException {
        Path config = tempDir.resolve("checkstyle.xml");
        Files.writeString(config, methodLengthConfig(2));
        Path javaFile = tempDir.resolve("BigMethod.java");
        Files.writeString(javaFile, longMethodSource(5));
        CheckstyleAnalyzer analyzer = new CheckstyleAnalyzer("checkstyle.xml");
        List<Violation> violations = analyzer.analyze(List.of(javaFile), tempDir);
        assertThat(violations).isNotEmpty();
        assertThat(violations.get(0).ruleId()).isEqualTo("checkstyle:MethodLength");
    }

    private static String minimalCheckstyleConfig() {
        return """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker">
                    <property name="severity" value="warning"/>
                    <module name="TreeWalker">
                    </module>
                </module>
                """;
    }

    private static String methodLengthConfig(int max) {
        return """
                <?xml version="1.0"?>
                <!DOCTYPE module PUBLIC
                    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
                    "https://checkstyle.org/dtds/configuration_1_3.dtd">
                <module name="Checker">
                    <property name="severity" value="warning"/>
                    <module name="TreeWalker">
                        <module name="MethodLength">
                            <property name="max" value="%d"/>
                        </module>
                    </module>
                </module>
                """.formatted(max);
    }

    private static String longMethodSource(int lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("public class BigMethod {\n");
        sb.append("    public void big() {\n");
        for (int i = 0; i < lines; i++) {
            sb.append("        int x").append(i).append(" = ").append(i).append(";\n");
        }
        sb.append("    }\n");
        sb.append("}\n");
        return sb.toString();
    }
}
