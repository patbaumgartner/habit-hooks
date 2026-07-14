package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Analyzer that runs a Taikai architecture-test class via Maven and turns each test
 * failure into a {@link Violation}.
 *
 * <p>
 * Taikai tests are project-wide, not file-scoped, so the {@code files} parameter of
 * {@link #analyze} is intentionally ignored — the whole architecture is checked on every
 * run.
 *
 * <p>
 * Lookup order for the Maven wrapper:
 * <ol>
 * <li>{@code ./mvnw} in the working directory (preferred)
 * <li>{@code mvn} on {@code PATH}
 * </ol>
 *
 * <p>
 * After the run, Surefire XML reports are parsed from
 * {@code target/surefire-reports/TEST-*<testClass>*.xml}. Each {@code <failure>} or
 * {@code <error>} element in a {@code <testcase>} becomes a violation with rule ID
 * {@code taikai:<testMethodName>}.
 */
public non-sealed class TaikaiAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaikaiAnalyzer.class);

    private static final String TOOL_PREFIX = "taikai";

    private final String testClass;

    /**
     * Creates an analyzer that invokes the given test class.
     * @param testClass simple name of the JUnit test class (e.g.
     * {@code "ArchitectureTest"})
     */
    public TaikaiAnalyzer(String testClass) {
        this.testClass = testClass;
    }

    @Override
    public String toolPrefix() {
        return TOOL_PREFIX;
    }

    /**
     * Returns {@code true} when a Maven wrapper or {@code mvn} command is present
     * <em>and</em> the test class exists somewhere under {@code src/test/java}.
     */
    @Override
    public boolean isAvailable(Path workingDir) {
        boolean hasMaven = Files.isRegularFile(workingDir.resolve("mvnw"))
                || Files.isRegularFile(workingDir.resolve("mvnw.cmd")) || MavenProcessRunner.commandExists("mvn");
        if (!hasMaven) {
            return false;
        }
        return findTestClass(workingDir).map(testFile -> hasRequiredTaikaiDependency(workingDir, testFile))
            .orElse(false);
    }

    private boolean hasRequiredTaikaiDependency(Path workingDir, Path testFile) {
        try {
            String source = Files.readString(testFile);
            return !source.contains("com.enofex.taikai") || List.of("pom.xml", "build.gradle", "build.gradle.kts")
                .stream()
                .map(workingDir::resolve)
                .anyMatch(buildFile -> buildFileContains(buildFile, "taikai"));
        }
        catch (IOException e) {
            LOGGER.error("Could not read {}: {}", testFile, e.getMessage(), e);
            return false;
        }
    }

    private Optional<Path> findTestClass(Path workingDir) {
        Path testRoot = workingDir.resolve("src/test/java");
        if (!Files.isDirectory(testRoot)) {
            return Optional.empty();
        }
        try (var stream = Files.walk(testRoot)) {
            return stream.filter(path -> {
                Path fileName = path.getFileName();
                return fileName != null && fileName.toString().equals(testClass + ".java");
            }).findFirst();
        }
        catch (IOException e) {
            LOGGER.error("Could not search for {}: {}", testClass, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static boolean buildFileContains(Path buildFile, String text) {
        if (!Files.isRegularFile(buildFile)) {
            return false;
        }
        try {
            return Files.readString(buildFile).contains(text);
        }
        catch (IOException e) {
            LOGGER.error("Could not read {}: {}", buildFile, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Runs the configured architecture test class via Maven and returns violations
     * derived from test failures. The {@code files} parameter is ignored.
     */
    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        int exitCode = runMaven(workingDir);
        String testFile = findTestClass(workingDir).map(workingDir::relativize)
            .map(Path::toString)
            .orElse(testClass + ".java");
        List<Violation> violations = new TaikaiReportParser(testClass).parse(workingDir, testFile);
        if (exitCode != 0 && violations.isEmpty()) {
            LOGGER.atWarn()
                .addArgument(testClass)
                .log("Taikai: Maven exited with errors running '{}'. Check that taikai is on the test classpath.");
        }
        return violations;
    }

    int runMaven(Path workingDir) {
        List<String> command = buildMavenCommand(workingDir);
        LOGGER.debug("Running Taikai: {}", command);
        try {
            int exitCode = waitForMaven(command, workingDir);
            LOGGER.debug("Taikai Maven exit code: {}", exitCode);
            return exitCode;
        }
        catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to run Taikai Maven command: {}", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }
    }

    private static int waitForMaven(List<String> command, Path workingDir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        drainOutput(process);
        return process.waitFor();
    }

    private static void drainOutput(Process process) throws IOException {
        try (InputStream out = process.getInputStream()) {
            out.transferTo(OutputStream.nullOutputStream());
        }
    }

    private List<String> buildMavenCommand(Path workingDir) {
        String mvn;
        if (Files.isRegularFile(workingDir.resolve("mvnw"))) {
            mvn = "./mvnw";
        }
        else if (Files.isRegularFile(workingDir.resolve("mvnw.cmd"))) {
            mvn = "mvnw.cmd";
        }
        else {
            mvn = "mvn";
        }
        return List.of(mvn, "-q", "test", "-Dtest=" + testClass, "-Dsurefire.failIfNoSpecifiedTests=false");
    }

}
