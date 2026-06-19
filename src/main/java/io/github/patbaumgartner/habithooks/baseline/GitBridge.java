package io.github.patbaumgartner.habithooks.baseline;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin wrapper around git CLI calls needed for baseline tracking.
 *
 * <p>
 * All methods return {@link Optional#empty()} or {@code false} when the git
 * command fails, so callers do not need to handle exceptions.
 */
public final class GitBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitBridge.class);

    private final Path workingDir;

    /**
     * Creates a bridge operating from the given directory.
     *
     * @param workingDir the git repository root
     */
    public GitBridge(Path workingDir) {
        this.workingDir = workingDir;
    }

    /**
     * Returns the hash of the last commit that touched the given file.
     *
     * @param filePath workspace-relative path to the file
     * @return the commit hash, or {@link Optional#empty()} on failure
     */
    public Optional<String> lastCommitHash(String filePath) {
        try {
            String output = exec("git", "log", "-1", "--format=%H", "--", filePath);
            String hash = output.strip();
            return hash.isEmpty() ? Optional.empty() : Optional.of(hash);
        } catch (IOException | InterruptedException e) {
            LOGGER.debug("Could not get last commit hash for {}: {}", filePath, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }

    /**
     * Returns {@code true} when the working tree has staged or unstaged changes
     * to the given file.
     *
     * @param filePath workspace-relative path to the file
     * @return {@code true} if the file has uncommitted changes
     */
    public boolean isDirty(String filePath) {
        try {
            String output = exec("git", "status", "--porcelain", "--", filePath);
            return !output.strip().isEmpty();
        } catch (IOException | InterruptedException e) {
            LOGGER.debug("Could not check dirty status for {}: {}", filePath, e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /**
     * Returns the list of files changed relative to the given branch base.
     *
     * @param branchBase the reference branch (e.g. {@code "main"})
     * @return newline-separated list of changed file paths, or empty string on
     *         failure
     */
    public String changedFilesSinceBranch(String branchBase) {
        try {
            return exec("git", "diff", "--name-only", branchBase + "...HEAD");
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Could not list changed files: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "";
        }
    }

    /**
     * Returns the list of files changed in the last {@code n} commits.
     *
     * @param n number of commits to look back
     * @return newline-separated list of changed file paths, or empty string on
     *         failure
     */
    public String changedFilesInLastN(int n) {
        try {
            return exec("git", "diff", "--name-only",
                    "HEAD~" + n + "...HEAD");
        } catch (IOException | InterruptedException e) {
            LOGGER.warn("Could not list changed files: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return "";
        }
    }

    private String exec(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir.toFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        process.waitFor();
        return output;
    }
}
