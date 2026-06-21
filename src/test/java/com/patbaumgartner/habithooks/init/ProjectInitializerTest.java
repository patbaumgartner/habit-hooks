package com.patbaumgartner.habithooks.init;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectInitializerTest {

    @TempDir
    Path tempDir;

    @Test
    void dryRunDoesNotWriteAnyFiles() {
        Output output = run(true, false);

        assertThat(output.text()).contains("[dry-run]");
        assertThat(Files.exists(tempDir.resolve(".habit-hooks.yaml"))).isFalse();
        assertThat(Files.exists(tempDir.resolve("checkstyle.xml"))).isFalse();
    }

    @Test
    void writesScaffoldFilesWhenAbsent() throws IOException {
        Output output = run(false, false);

        assertThat(output.text()).contains("initialized");
        assertThat(output.text()).contains("habit-hooks --all");
        assertThat(Files.exists(tempDir.resolve("checkstyle.xml"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("pmd-ruleset.xml"))).isTrue();
        assertThat(Files.exists(tempDir.resolve(".habit-hooks.yaml"))).isTrue();
        assertThat(Files.exists(tempDir.resolve(".habit-hooks-baseline.json"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("habit-hooks-maven-snippets.xml"))).isFalse();
        assertThat(Files.readString(tempDir.resolve(".habit-hooks.yaml"))).contains("spotbugs:", "spring-javaformat:");
        assertThat(Files.readString(tempDir.resolve("checkstyle.xml"))).doesNotContain("FileTabCharacter",
                "EmptyLineSeparator");
    }

    @Test
    void mavenSnippetsAreOptIn() throws IOException {
        run(false, false, true);

        Path snippets = tempDir.resolve("habit-hooks-maven-snippets.xml");
        assertThat(Files.exists(snippets)).isTrue();
        String content = Files.readString(snippets);
        assertThat(content).contains("spotbugs-maven-plugin");
        assertThat(content).contains("jacoco-maven-plugin");
        assertThat(content).contains("org.jspecify");
        assertThat(content).contains("com.enofex");
    }

    @Test
    void skipsFilesThatAlreadyExist() throws IOException {
        Files.writeString(tempDir.resolve("checkstyle.xml"), "<module/>");

        Output output = run(false, false);

        assertThat(output.text()).contains("skip  checkstyle.xml (already exists)");
        assertThat(Files.readString(tempDir.resolve("checkstyle.xml"))).isEqualTo("<module/>");
    }

    @Test
    void taikaiScaffoldsArchitectureTestWhenTestDirExists() throws IOException {
        Files.createDirectories(tempDir.resolve("src/test/java"));

        Output output = run(false, true);
        String architectureTest = Files.readString(tempDir.resolve("src/test/java/ArchitectureTest.java"));

        assertThat(output.text()).contains("Taikai tests require the com.enofex:taikai test dependency");
        assertThat(Files.exists(tempDir.resolve("src/test/java/ArchitectureTest.java"))).isTrue();
        assertThat(architectureTest).contains("\tprivate static final String NAMESPACE");
        assertThat(architectureTest).contains("// change this").doesNotContain("←");
    }

    @Test
    void taikaiIsSkippedWhenNoTestDirectory() {
        Output output = run(false, true);

        assertThat(output.text()).contains("no src/test/java found");
        assertThat(Files.exists(tempDir.resolve("src/test/java/ArchitectureTest.java"))).isFalse();
    }

    private Output run(boolean dryRun, boolean taikai) {
        return run(dryRun, taikai, false);
    }

    private Output run(boolean dryRun, boolean taikai, boolean mavenSnippets) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        ProjectInitializer.Options options = new ProjectInitializer.Options(dryRun, taikai, mavenSnippets);
        new ProjectInitializer(tempDir, options, out).initialize();
        return new Output(buffer.toString(StandardCharsets.UTF_8));
    }

    private record Output(String text) {
    }

}
