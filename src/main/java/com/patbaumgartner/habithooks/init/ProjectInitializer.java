package com.patbaumgartner.habithooks.init;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scaffolds habit-hooks configuration files in a new project.
 *
 * <p>
 * Detects existing tool configurations, scaffolds missing ones, and writes
 * {@code .habit-hooks.yaml} plus {@code AGENTS.md} guidance. When {@code taikai} or
 * {@code springBoot} is set it also writes an {@code ArchitectureTest.java} template.
 * When {@code mavenSnippets} or {@code springBoot} is set it writes optional Maven plugin
 * and dependency snippets for project-scoped analyzers.
 */
public final class ProjectInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectInitializer.class);

    private static final String TEMPLATES_PREFIX = "com/patbaumgartner/habithooks/templates/";

    private final Path workingDir;

    private final Options options;

    private final Output out;

    /**
     * Creates an initializer for the given project.
     * @param workingDir the project root
     * @param options scaffold options
     * @param out the stream to print progress messages to
     */
    public ProjectInitializer(Path workingDir, Options options, PrintStream out) {
        this.workingDir = workingDir;
        this.options = options;
        this.out = out::printf;
    }

    /** Runs the initialization process. */
    public void initialize() {
        if (options.dryRun()) {
            out.println("[dry-run] The following files would be written:");
        }
        if (options.agentsOnly()) {
            writeIfAbsent("AGENTS.md", agentsTemplate());
            printAgentsOnlyCompletion();
            return;
        }
        scaffoldAll();
        printCompletion();
    }

    private void scaffoldAll() {
        writeIfAbsent("checkstyle.xml", "checkstyle.xml");
        writeIfAbsent("pmd-ruleset.xml", "pmd-ruleset.xml");
        writeIfAbsent(".habit-hooks.yaml", configTemplate());
        writeIfAbsent(".habit-hooks-baseline.json", "baseline-empty.json");
        writeIfAbsent("AGENTS.md", agentsTemplate());
        if (options.taikai() || options.springBoot()) {
            scaffoldTaikaiTest();
        }
        if (options.mavenSnippets() || options.springBoot()) {
            writeIfAbsent("habit-hooks-maven-snippets.xml", "maven-quality-pom-snippets.xml");
        }
    }

    private String configTemplate() {
        if (options.springBoot()) {
            return "spring-boot-habit-hooks-config.yaml";
        }
        return "habit-hooks-config.yaml";
    }

    private String agentsTemplate() {
        if (options.springBoot()) {
            return "spring-boot-agents.md.template";
        }
        return "agents.md.template";
    }

    private void printAgentsOnlyCompletion() {
        if (!options.dryRun()) {
            out.println("✅ AGENTS.md scaffolded. Commit it so coding agents pick up the habit-hooks workflow.");
        }
    }

    private void printCompletion() {
        if (!options.dryRun()) {
            if (options.taikai() || options.springBoot()) {
                out.println("ℹ️  Taikai tests require the com.enofex:taikai test dependency."
                        + " Copy it from habit-hooks-maven-snippets.xml before running Maven tests.");
            }
            if (options.springBoot()) {
                out.println("ℹ️  Spring Boot analyzers were enabled. Run habit-hooks doctor after copying Maven"
                        + " snippets into pom.xml.");
            }
            out.println("✅ habit-hooks initialized. Run: habit-hooks --all");
        }
    }

    private void writeIfAbsent(String targetFile, String templateResource) {
        Path target = workingDir.resolve(targetFile);
        if (Files.exists(target)) {
            out.printf("  skip  %s (already exists)%n", targetFile);
            return;
        }
        Optional<String> content = loadTemplate(templateResource);
        if (content.isEmpty()) {
            LOGGER.warn("Template not found: {}", templateResource);
            return;
        }
        if (options.dryRun()) {
            out.printf("  write %s%n", targetFile);
            return;
        }
        writeFile(target, content.get());
        out.printf("  wrote %s%n", targetFile);
    }

    private void scaffoldTaikaiTest() {
        Path testDir = workingDir.resolve("src/test/java");
        if (!Files.isDirectory(testDir)) {
            out.printf("  skip  ArchitectureTest.java (no src/test/java found)%n");
            return;
        }
        writeIfAbsent("src/test/java/ArchitectureTest.java", "ArchitectureTest.java.template");
    }

    private Optional<String> loadTemplate(String resource) {
        String path = TEMPLATES_PREFIX + resource;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return Optional.empty();
            }
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            LOGGER.error("Failed to load template {}: {}", path, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private void writeFile(Path target, String content) {
        try {
            createParentDirectories(target);
            Files.writeString(target, content, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            LOGGER.error("Failed to write {}: {}", target, e.getMessage(), e);
        }
    }

    private static void createParentDirectories(Path target) throws IOException {
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /** Options controlling which optional scaffold files are written. */
    public record Options(boolean dryRun, boolean taikai, boolean mavenSnippets, boolean springBoot,
            boolean agentsOnly) {
    }

    @FunctionalInterface
    private interface Output {

        void printf(String format, Object... args);

        default void println(String line) {
            printf("%s%n", line);
        }

    }

}
