package io.github.patbaumgartner.habithooks.cli;

import io.github.patbaumgartner.habithooks.init.ProjectInitializer;
import java.nio.file.Path;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * CLI sub-command that scaffolds a habit-hooks configuration in the current
 * project.
 *
 * <p>
 * Detects existing Checkstyle/PMD configurations, scaffolds missing ones,
 * writes {@code .habit-hooks.yaml}, an empty baseline, and optionally an
 * {@code ArchitectureTest.java} powered by Taikai.
 */
@Command(name = "init", description = "Scaffold habit-hooks configuration for this project")
public final class InitCommand implements Runnable {

    @ParentCommand
    private HabitHooksCommand parent;

    @Option(names = { "--dry-run" }, description = "Print intended writes without touching disk")
    private boolean dryRun;

    @Option(names = { "--taikai" }, description = "Also scaffold a Taikai architecture test")
    private boolean taikai;

    @Override
    public void run() {
        Path workingDir = parent.workingDir();
        ProjectInitializer initializer = new ProjectInitializer(workingDir, dryRun, taikai, System.out);
        initializer.initialize();
    }
}
