package io.github.patbaumgartner.habithooks.cli;

import io.github.patbaumgartner.habithooks.baseline.BaselineManager;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Path;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;

/**
 * CLI sub-command group for baseline management operations.
 */
@Command(name = "baseline", description = "Manage the violation baseline", subcommands = {
        BaselineCommand.SnoozeSubcommand.class,
        BaselineCommand.PruneSubcommand.class,
        BaselineCommand.StatusSubcommand.class
})
public final class BaselineCommand implements Runnable {

    @ParentCommand
    private HabitHooksCommand parent;

    @Override
    public void run() {
        System.out.println("Usage: habit-hooks baseline <snooze|prune|status>");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Sub-commands
    // ──────────────────────────────────────────────────────────────────────────

    /** Adds current violations to the baseline. */
    @Command(name = "snooze", description = "Add current violations to the baseline")
    static final class SnoozeSubcommand implements Runnable {

        @ParentCommand
        private BaselineCommand baseline;

        @Override
        public void run() {
            Path workingDir = baseline.parent.workingDir();
            List<Violation> violations = baseline.parent.runAnalysis(workingDir);
            BaselineManager mgr = new BaselineManager(workingDir);
            mgr.snooze(violations);
            System.out.println("Baseline updated. "
                    + violations.size() + " violation(s) snoozed.");
        }
    }

    /** Removes baseline entries for files that no longer exist. */
    @Command(name = "prune", description = "Remove baseline entries for deleted files")
    static final class PruneSubcommand implements Runnable {

        @ParentCommand
        private BaselineCommand baseline;

        @Override
        public void run() {
            Path workingDir = baseline.parent.workingDir();
            new BaselineManager(workingDir).prune();
            System.out.println("Baseline pruned.");
        }
    }

    /** Prints a summary of the current baseline. */
    @Command(name = "status", description = "Summarise the current baseline")
    static final class StatusSubcommand implements Runnable {

        @ParentCommand
        private BaselineCommand baseline;

        @Override
        public void run() {
            Path workingDir = baseline.parent.workingDir();
            String summary = new BaselineManager(workingDir).status();
            System.out.println(summary);
        }
    }
}
