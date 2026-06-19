package io.github.patbaumgartner.habithooks;

import io.github.patbaumgartner.habithooks.cli.HabitHooksCommand;
import picocli.CommandLine;

/**
 * Main entry point for habit-hooks.
 *
 * <p>Delegates entirely to the picocli command tree rooted at
 * {@link HabitHooksCommand}.
 */
public final class HabitHooks {

    private HabitHooks() {
        // utility class
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new HabitHooksCommand())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
        System.exit(exitCode);
    }
}
