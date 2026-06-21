package com.patbaumgartner.habithooks.update;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Describes the installed habit-hooks artifact that {@code --update} should replace.
 *
 * @param os the normalized operating-system token (e.g. {@code linux}, {@code darwin})
 * @param arch the normalized architecture token (e.g. {@code x64}, {@code arm64})
 * @param nativeImage whether the running process is a GraalVM native image
 * @param artifact the path of the installed artifact to replace, if it can be located
 */
public record InstallTarget(String os, String arch, boolean nativeImage, Optional<Path> artifact) {

    private static final String UNSUPPORTED = "unsupported";

    /**
     * Detects the install target for the currently running process.
     * @return the resolved install target
     */
    public static InstallTarget detect() {
        boolean nativeImage = isNativeImage();
        return new InstallTarget(osToken(), archToken(), nativeImage, locateArtifact(nativeImage));
    }

    /**
     * Returns the release asset name for this target.
     * @return the native binary asset name, or the launcher JAR name on the JVM
     */
    public String assetName() {
        if (this.nativeImage) {
            return "habit-hooks-" + this.os + "-" + this.arch;
        }
        return "habit-hooks-launcher.jar";
    }

    /**
     * Indicates whether the platform can receive a native-binary update.
     * @return {@code true} when both OS and architecture are recognized
     */
    public boolean supported() {
        return !UNSUPPORTED.equals(this.os) && !UNSUPPORTED.equals(this.arch);
    }

    private static boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    private static String osToken() {
        String name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (name.contains("mac") || name.contains("darwin")) {
            return "darwin";
        }
        if (name.contains("linux")) {
            return "linux";
        }
        return UNSUPPORTED;
    }

    private static String archToken() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if ("amd64".equals(arch) || "x86_64".equals(arch)) {
            return "x64";
        }
        if ("aarch64".equals(arch) || "arm64".equals(arch)) {
            return "arm64";
        }
        return UNSUPPORTED;
    }

    private static Optional<Path> locateArtifact(boolean nativeImage) {
        return nativeImage ? currentExecutable() : currentJar();
    }

    private static Optional<Path> currentExecutable() {
        return ProcessHandle.current().info().command().map(Path::of);
    }

    private static Optional<Path> currentJar() {
        try {
            Path path = Path.of(InstallTarget.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return path.toString().endsWith(".jar") ? Optional.of(path) : Optional.empty();
        }
        catch (URISyntaxException | RuntimeException ex) {
            return Optional.empty();
        }
    }

}
