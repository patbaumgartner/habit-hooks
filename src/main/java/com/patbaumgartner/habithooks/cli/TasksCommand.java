package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.tasks.AgentTaskExporter;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/** Exports findings as small, agent-ready implementation tasks. */
@Command(name = "tasks", mixinStandardHelpOptions = true,
        description = "Export analyzer findings as agent task batches.")
final class TasksCommand implements Callable<Integer> {

    @ParentCommand
    private HabitHooksCommand parent;

    @Option(names = { "--format" }, description = "markdown, md, or json", defaultValue = "markdown",
            paramLabel = "<format>")
    private String format;

    @Option(names = { "--output" }, description = "Output directory, relative to the project root",
            defaultValue = "target/habit-hooks", paramLabel = "<dir>")
    private Path outputDir;

    @Option(names = { "--no-fail" }, description = "Always exit 0 after writing the task export")
    private boolean noFail;

    @Override
    public Integer call() throws Exception {
        AgentTaskExporter.Format normalizedFormat = parseFormat();
        if (normalizedFormat == null) {
            return 2;
        }
        Path workingDir = parent.workingDir();
        AnalysisRun run = parent.analyzeConfigured(workingDir);
        if (run.skipped()) {
            System.out.println(run.skipMessage());
            return 0;
        }
        Path output = new AgentTaskExporter().write(run.result(), resolveOutputDir(workingDir), normalizedFormat);
        System.out.println("Wrote " + output);
        return noFail || !run.hasFailures() ? 0 : 1;
    }

    private AgentTaskExporter.Format parseFormat() {
        try {
            return AgentTaskExporter.Format.parse(format);
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }

    private Path resolveOutputDir(Path workingDir) {
        return outputDir.isAbsolute() ? outputDir : workingDir.resolve(outputDir);
    }

}
