package com.patbaumgartner.habithooks.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class MavenProcessRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProcessRunner.class);

    private static final String MAVEN_WRAPPER = "mvnw";

    private final String toolPrefix;

    private final String goal;

    MavenProcessRunner(String toolPrefix, String goal) {
        this.toolPrefix = toolPrefix;
        this.goal = goal;
    }

    boolean isAvailable(Path workingDir) {
        return Files.isRegularFile(workingDir.resolve(MAVEN_WRAPPER)) || commandExists("mvn");
    }

    ExecutionResult run(Path workingDir) {
        List<String> command = buildCommand(workingDir);
        LOGGER.debug("Running {} analyzer: {}", toolPrefix, command);
        try {
            Process process = start(command, workingDir);
            String output = readOutput(process);
            return new ExecutionResult(process.waitFor(), output);
        }
        catch (IOException | InterruptedException ex) {
            return failure(ex);
        }
    }

    static boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").redirectErrorStream(true).start();
            drain(process);
            return process.waitFor() == 0;
        }
        catch (IOException | InterruptedException ex) {
            interruptIfNeeded(ex);
            return false;
        }
    }

    private ExecutionResult failure(Exception ex) {
        LOGGER.error("Failed to run {} Maven goal '{}': {}", toolPrefix, goal, ex.getMessage(), ex);
        interruptIfNeeded(ex);
        return new ExecutionResult(-1, ex.getMessage());
    }

    private List<String> buildCommand(Path workingDir) {
        String mvn = Files.isRegularFile(workingDir.resolve(MAVEN_WRAPPER)) ? "./" + MAVEN_WRAPPER : "mvn";
        List<String> command = new ArrayList<>();
        command.add(mvn);
        command.add("--batch-mode");
        command.add("--no-transfer-progress");
        command.addAll(List.of(goal.split("\\s+")));
        return List.copyOf(command);
    }

    private static Process start(List<String> command, Path workingDir) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private static String readOutput(Process process) throws IOException {
        try (InputStream out = process.getInputStream()) {
            return new String(out.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void drain(Process process) throws IOException {
        try (InputStream out = process.getInputStream()) {
            out.transferTo(OutputStream.nullOutputStream());
        }
    }

    private static void interruptIfNeeded(Exception ex) {
        if (ex instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

}
