package io.github.patbaumgartner.habithooks.scope;

import io.github.patbaumgartner.habithooks.baseline.GitBridge;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves the set of Java source files to include in an analysis run based on
 * the selected scope mode.
 */
public final class FileScope {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScope.class);
    private static final String JAVA_EXTENSION = ".java";

    private final Path workingDir;
    private final GitBridge git;

    /**
     * Creates a resolver for the given project root.
     *
     * @param workingDir the project root
     */
    public FileScope(Path workingDir) {
        this(workingDir, new GitBridge(workingDir));
    }

    FileScope(Path workingDir, GitBridge git) {
        this.workingDir = workingDir;
        this.git = git;
    }

    /**
     * Resolves all {@code .java} files under the project root (recursively).
     *
     * @return the list of Java source files
     */
    public List<Path> allFiles() {
        return allFiles(true);
    }

    /**
     * Resolves all {@code .java} files under the project root (recursively).
     *
     * @param excludeTests when {@code true}, files under {@code src/test/} are
     *                     excluded
     * @return the list of Java source files
     */
    public List<Path> allFiles(boolean excludeTests) {
        try (Stream<Path> walk = Files.walk(workingDir)) {
            return walk
                    .filter(p -> p.toString().endsWith(JAVA_EXTENSION))
                    .filter(p -> !isInTarget(p))
                    .filter(p -> !excludeTests || !isTestSource(p))
                    .toList();
        } catch (IOException e) {
            LOGGER.warn("Failed to walk project directory: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Resolves files changed since the given branch base.
     *
     * @param branchBase the git branch to diff against (e.g. {@code "main"})
     * @return the list of changed Java source files
     */
    public List<Path> changedSinceBranch(String branchBase) {
        String output = git.changedFilesSinceBranch(branchBase);
        return parseChangedFiles(output);
    }

    /**
     * Resolves files changed in the last {@code n} commits.
     *
     * @param n number of commits
     * @return the list of changed Java source files
     */
    public List<Path> changedInLastN(int n) {
        String output = git.changedFilesInLastN(n);
        return parseChangedFiles(output);
    }

    private List<Path> parseChangedFiles(String gitOutput) {
        if (gitOutput.isBlank()) {
            return List.of();
        }
        return Arrays.stream(gitOutput.split("\n"))
                .map(String::strip)
                .filter(s -> s.endsWith(JAVA_EXTENSION))
                .map(workingDir::resolve)
                .filter(Files::isRegularFile)
                .toList();
    }

    private static boolean isInTarget(Path path) {
        return path.toString().contains("/target/")
                || path.toString().contains("\\target\\");
    }

    private static boolean isTestSource(Path path) {
        return path.toString().contains("/src/test/")
                || path.toString().contains("\\src\\test\\");
    }
}
