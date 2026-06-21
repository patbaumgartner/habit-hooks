package com.patbaumgartner.habithooks.cli;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/**
 * Reports or applies Maven dependency updates with the Maven Versions Plugin.
 */
@Command(name = "dependencies", mixinStandardHelpOptions = true,
        description = "Report or apply Maven dependency and plugin updates.")
final class DependenciesCommand implements Callable<Integer> {

    @ParentCommand
    private HabitHooksCommand parent;

    @Option(names = { "--apply" }, description = "Apply parent/property updates instead of reporting only")
    private boolean apply;

    @Option(names = { "--allow-major" }, description = "Allow major updates when --apply is used")
    private boolean allowMajor;

    @Option(names = { "--output" }, description = "Report output file, relative to the project root",
            defaultValue = "target/habit-hooks/dependencies.txt", paramLabel = "<file>")
    private Path output;

    @Override
    public Integer call() throws Exception {
        Path workingDir = parent.workingDir();
        Path resolvedOutput = resolveOutput(workingDir);
        List<String> command = apply ? applyCommand(workingDir) : reportCommand(workingDir);
        Process process = start(command, workingDir);
        String text = readOutput(process);
        createOutputParent(resolvedOutput);
        Files.writeString(resolvedOutput, text, StandardCharsets.UTF_8);
        System.out.println(text.strip().isBlank() ? "No dependency output." : text.strip());
        System.out.println("Wrote " + resolvedOutput);
        return process.waitFor();
    }

    private Path resolveOutput(Path workingDir) {
        return output.isAbsolute() ? output : workingDir.resolve(output);
    }

    private void createOutputParent(Path resolvedOutput) throws Exception {
        Path parentDir = resolvedOutput.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    private List<String> reportCommand(Path workingDir) {
        List<String> command = baseCommand(workingDir);
        command.add("versions:display-parent-updates");
        command.add("versions:display-property-updates");
        command.add("versions:display-plugin-updates");
        return List.copyOf(command);
    }

    private List<String> applyCommand(Path workingDir) {
        List<String> command = baseCommand(workingDir);
        command.add("versions:update-parent");
        command.add("versions:update-properties");
        command.add("-DgenerateBackupPoms=false");
        command.add("-DallowMajorUpdates=" + allowMajor);
        return List.copyOf(command);
    }

    private List<String> baseCommand(Path workingDir) {
        List<String> command = new ArrayList<>();
        command.add(Files.isRegularFile(workingDir.resolve("mvnw")) ? "./mvnw" : "mvn");
        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        return command;
    }

    private static Process start(List<String> command, Path workingDir) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workingDir.toFile());
        builder.redirectErrorStream(true);
        return builder.start();
    }

    private static String readOutput(Process process) throws Exception {
        try (InputStream stream = process.getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
