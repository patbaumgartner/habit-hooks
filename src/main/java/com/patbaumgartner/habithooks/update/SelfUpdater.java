package com.patbaumgartner.habithooks.update;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Downloads the latest published habit-hooks release and replaces the running artifact in
 * place, powering the {@code habit-hooks --update} command.
 */
public final class SelfUpdater {

    private static final String REPO = "patbaumgartner/habit-hooks";

    private static final String API_LATEST = "https://api.github.com/repos/" + REPO + "/releases/latest";

    private static final String DOWNLOAD_BASE = "https://github.com/" + REPO + "/releases/download/";

    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    private final String currentVersion;

    private final PrintStream out;

    private final HttpFetcher fetcher;

    private final InstallTarget target;

    /**
     * Creates an updater using the JDK HTTP client and the detected install target.
     * @param currentVersion the version of the running build
     * @param out the stream for progress messages
     */
    public SelfUpdater(String currentVersion, PrintStream out) {
        this(currentVersion, out, new JdkHttpFetcher(), InstallTarget.detect());
    }

    SelfUpdater(String currentVersion, PrintStream out, HttpFetcher fetcher, InstallTarget target) {
        this.currentVersion = currentVersion;
        this.out = out;
        this.fetcher = fetcher;
        this.target = target;
    }

    /**
     * Runs the self-update flow.
     * @return {@code 0} on success or when already up to date, {@code 1} on failure
     */
    public int run() {
        Optional<String> tag = latestTag();
        if (tag.isEmpty()) {
            this.out.println("Could not determine the latest release from GitHub.");
            return 1;
        }
        String latestVersion = normalize(tag.get());
        if (latestVersion.equals(normalize(this.currentVersion))) {
            this.out.printf("Already up to date (%s).%n", this.currentVersion);
            return 0;
        }
        return install(tag.get(), latestVersion);
    }

    private Optional<String> latestTag() {
        try {
            return parseTag(this.fetcher.getText(API_LATEST));
        }
        catch (IOException ex) {
            return Optional.empty();
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private int install(String tag, String latestVersion) {
        Optional<Path> artifact = this.target.artifact();
        if (artifact.isEmpty()) {
            return missingArtifact();
        }
        String url = DOWNLOAD_BASE + tag + "/" + this.target.assetName();
        this.out.printf("Updating habit-hooks %s -> %s ...%n", this.currentVersion, latestVersion);
        return replaceAndReport(latestVersion, url, artifact.get());
    }

    private int missingArtifact() {
        this.out.println("Cannot locate the installed habit-hooks artifact to update.");
        return 1;
    }

    private int replaceAndReport(String latestVersion, String url, Path artifact) {
        try {
            replaceArtifact(url, artifact);
        }
        catch (IOException ex) {
            this.out.println("Update failed: " + ex.getMessage());
            return 1;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            this.out.println("Update interrupted.");
            return 1;
        }
        this.out.printf("Updated to %s. Re-run habit-hooks to use the new version.%n", latestVersion);
        return 0;
    }

    private void replaceArtifact(String url, Path artifact) throws IOException, InterruptedException {
        Path directory = parentDirectory(artifact);
        Path temp = Files.createTempFile(directory, ".habit-hooks-update", ".tmp");
        try {
            this.fetcher.downloadTo(url, temp);
            if (this.target.nativeImage()) {
                makeExecutable(temp);
            }
            move(temp, artifact);
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    private static Path parentDirectory(Path artifact) {
        Path parent = artifact.toAbsolutePath().getParent();
        return parent == null ? Path.of(".").toAbsolutePath() : parent;
    }

    static Optional<String> parseTag(String json) {
        Matcher matcher = TAG_PATTERN.matcher(json);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    static String normalize(String version) {
        String trimmed = version.strip();
        return trimmed.startsWith("v") ? trimmed.substring(1) : trimmed;
    }

    private static void move(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void makeExecutable(Path path) throws IOException {
        try {
            Set<PosixFilePermission> permissions = new HashSet<>(Files.getPosixFilePermissions(path));
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(path, permissions);
        }
        catch (UnsupportedOperationException ex) {
            makeExecutableWithFileApi(path);
        }
    }

    private static void makeExecutableWithFileApi(Path path) {
        if (!path.toFile().setExecutable(true, false)) {
            path.toFile().setExecutable(true, true);
        }
    }

}
