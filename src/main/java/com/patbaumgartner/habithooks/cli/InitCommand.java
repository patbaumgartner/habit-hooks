package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.init.ProjectInitializer;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * CLI sub-command that scaffolds a habit-hooks configuration in the current project.
 *
 * <p>
 * Detects existing Checkstyle/PMD configurations, scaffolds missing ones, writes
 * {@code .habit-hooks.yaml}, {@code AGENTS.md}, an empty baseline, and optionally an
 * {@code ArchitectureTest.java} powered by Taikai or Maven snippets for optional
 * project-scoped analyzers. Spring Boot mode enables the full project-scoped analyzer set
 * and scaffolds the supporting files.
 */
@Command(name = "init", mixinStandardHelpOptions = true,
        description = "Scaffold habit-hooks configuration for this project")
public final class InitCommand implements Runnable {

    @ParentCommand
    private HabitHooksCommand parent;

    @Option(names = { "--dry-run" }, description = "Print intended writes without touching disk")
    private boolean dryRun;

    @Option(names = { "--taikai" }, description = "Also scaffold a Taikai architecture test")
    private boolean taikai;

    @Option(names = { "--maven-snippets" }, description = "Also scaffold optional Maven plugin snippets")
    private boolean mavenSnippets;

    @Option(names = { "--spring-boot" }, description = "Enable Spring Boot analyzer defaults and support files")
    private boolean springBoot;

    @Option(names = { "--agents" }, description = "Only scaffold AGENTS.md, leaving other files untouched")
    private boolean agentsOnly;

    @Override
    public void run() {
        Path workingDir = parent.workingDir();
        ProjectInitializer.Options options = new ProjectInitializer.Options(dryRun, taikai, mavenSnippets, springBoot,
                agentsOnly);
        ProjectInitializer initializer = new ProjectInitializer(workingDir, options, System.out);
        initializer.initialize();
    }

}
