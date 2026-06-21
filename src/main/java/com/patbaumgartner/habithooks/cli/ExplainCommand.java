package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.coaching.PromptLoader;
import com.patbaumgartner.habithooks.coaching.RuleTitles;
import com.patbaumgartner.habithooks.config.ConfigLoader;
import com.patbaumgartner.habithooks.config.HabitHooksConfig;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * CLI sub-command that prints the coaching guidance for a single rule on demand.
 *
 * <p>
 * Unlike a full run, {@code explain} does not need a live violation: it resolves the same
 * prompt the coaching engine would show, honoring any project-local prompts directory.
 */
@Command(name = "explain", mixinStandardHelpOptions = true,
        description = "Print the coaching guidance for a rule, e.g. habit-hooks explain pmd:GodClass")
final class ExplainCommand implements Callable<Integer> {

    @ParentCommand
    private HabitHooksCommand parent;

    @Parameters(index = "0", paramLabel = "<ruleId>", description = "Rule ID to explain, e.g. checkstyle:MethodLength")
    private String ruleId;

    @Override
    public Integer call() {
        Path workingDir = parent.workingDir();
        HabitHooksConfig config = ConfigLoader.load(parent.configPath(), workingDir);
        PromptLoader promptLoader = new PromptLoader(workingDir.resolve(config.getPromptsDir()));
        Optional<String> prompt = promptLoader.load(ruleId);

        System.out.printf("%s (%s)%n%n", RuleTitles.titleFor(ruleId), ruleId);
        if (prompt.isEmpty()) {
            System.out.printf("No coaching prompt is bundled for %s yet.%n", ruleId);
            System.out.println("Add one as a <tool>-<RuleName>.md file in your prompts directory.");
            return 1;
        }
        System.out.println(prompt.get().strip());
        return 0;
    }

}
