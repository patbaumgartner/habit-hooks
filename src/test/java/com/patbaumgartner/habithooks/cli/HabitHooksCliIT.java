package com.patbaumgartner.habithooks.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.assertThat;

class HabitHooksCliIT {

    @TempDir
    Path tempDir;

    @Timeout(60)
    @Test
    void runAllOnCleanProjectReturnsZero() throws IOException {
        prepareProject(tempDir, false);

        CommandResult result = execute(tempDir, "--all");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("all checks passed");
    }

    @Test
    void baselineSnoozeThenRunAllSuppressesExistingViolations() throws IOException, InterruptedException {
        prepareProject(tempDir, true);
        initializeGitRepo(tempDir);

        CommandResult firstRun = execute(tempDir, "--all");
        assertThat(firstRun.exitCode()).isEqualTo(1);

        CommandResult snooze = execute(tempDir, "baseline", "snooze");
        assertThat(snooze.exitCode()).isZero();

        CommandResult secondRun = execute(tempDir, "--all");
        assertThat(secondRun.exitCode()).isZero();
        assertThat(secondRun.output()).contains("all checks passed");
    }

    @Test
    void reportWritesMarkdownWithoutFailingWhenRequested() throws IOException {
        prepareProject(tempDir, true);

        CommandResult result = execute(tempDir, "report", "--no-fail");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("report.md");
        assertThat(Files.exists(tempDir.resolve("target/habit-hooks/report.md"))).isTrue();
    }

    @Test
    void tasksWritesJsonWithoutFailingWhenRequested() throws IOException {
        prepareProject(tempDir, true);

        CommandResult result = execute(tempDir, "tasks", "--format", "json", "--no-fail");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("tasks.json");
        assertThat(Files.readString(tempDir.resolve("target/habit-hooks/tasks.json"))).contains("verificationCommand");
    }

    @Test
    void invalidReportFormatReturnsUsageError() throws IOException {
        prepareProject(tempDir, true);

        CommandResult result = execute(tempDir, "report", "--format", "xml");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.output()).contains("Unsupported report format");
    }

    @Test
    void dependenciesWriteRelativeOutputInProjectRoot() throws IOException {
        writeMavenWrapper(tempDir);

        CommandResult result = execute(tempDir, "dependencies", "--output", "reports/dependencies.txt");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("Wrote", "reports/dependencies.txt");
        assertThat(Files.readString(tempDir.resolve("reports/dependencies.txt"))).contains("dependency report");
    }

    @Test
    void explainPrintsCoachingForKnownRule() {
        CommandResult result = execute(tempDir, "explain", "pmd:GodClass");

        assertThat(result.exitCode()).isZero();
        assertThat(result.output()).contains("God Class", "pmd:GodClass");
    }

    @Test
    void explainReturnsErrorForUncoachedRule() {
        CommandResult result = execute(tempDir, "explain", "pmd:DoesNotExist");

        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.output()).contains("No coaching prompt");
    }

    private static void prepareProject(Path projectDir, boolean withViolation) throws IOException {
        Path sourceDir = projectDir.resolve("src/main/java/com/example");
        Files.createDirectories(sourceDir);
        writeProjectConfig(projectDir);
        writeSource(projectDir, withViolation);
    }

    private static void writeProjectConfig(Path projectDir) throws IOException {
        Path repoRoot = Path.of(System.getProperty("user.dir", "."));
        Files.copy(repoRoot.resolve("checkstyle.xml"), projectDir.resolve("checkstyle.xml"));
        Files.copy(repoRoot.resolve("pmd-ruleset.xml"), projectDir.resolve("pmd-ruleset.xml"));
        Files.writeString(projectDir.resolve(".habit-hooks.yaml"), """
                prompts: ./prompts

                scope:
                  onlyChangedFiles: false
                  branchBase: main
                  excludeTests: true

                analyzers:
                  checkstyle:
                    enabled: true
                    configFile: checkstyle.xml
                  pmd:
                    enabled: true
                    rulesets:
                      - pmd-ruleset.xml
                """, StandardCharsets.UTF_8);
        Files.writeString(projectDir.resolve(".habit-hooks-baseline.json"), "{}", StandardCharsets.UTF_8);
    }

    private static void writeSource(Path projectDir, boolean withViolation) throws IOException {
        String source = withViolation ? violatingClass() : cleanClass();
        Files.writeString(projectDir.resolve("src/main/java/com/example/Example.java"), source, StandardCharsets.UTF_8);
    }

    private static String cleanClass() {
        return """
                package com.example;

                public final class Example {

                    public int add(int a, int b) {
                        return a + b;
                    }
                }
                """;
    }

    private static String violatingClass() {
        return """
                package com.example;

                public final class Example {

                    public boolean complex(int a, int b, int c, int d, int e) {
                        return (a > 1 && b > 2 && c > 3) || (d > 4 && e > 5 && a > 0);
                    }
                }
                """;
    }

    private static void initializeGitRepo(Path projectDir) throws IOException, InterruptedException {
        runGit(projectDir, "git", "init", "-b", "main");
        runGit(projectDir, "git", "config", "user.email", "it@example.com");
        runGit(projectDir, "git", "config", "user.name", "integration-test");
        runGit(projectDir, "git", "add", ".");
        runGit(projectDir, "git", "commit", "-m", "initial");
    }

    private static void runGit(Path projectDir, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).directory(projectDir.toFile()).start();
        int exitCode = process.waitFor();
        assertThat(exitCode).isZero();
    }

    private static void writeMavenWrapper(Path projectDir) throws IOException {
        Path wrapper = projectDir.resolve("mvnw");
        Files.writeString(wrapper, "#!/bin/sh\necho dependency report\n", StandardCharsets.UTF_8);
        assertThat(wrapper.toFile().setExecutable(true)).isTrue();
    }

    private static CommandResult execute(Path workingDir, String... args) {
        String originalUserDir = System.getProperty("user.dir", ".");
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setProperty("user.dir", workingDir.toString());
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(out, true, StandardCharsets.UTF_8));
            int exitCode = new CommandLine(new HabitHooksCommand()).execute(args);
            return new CommandResult(exitCode, out.toString(StandardCharsets.UTF_8));
        }
        finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setProperty("user.dir", originalUserDir);
        }
    }

    private record CommandResult(int exitCode, String output) {
    }

}
