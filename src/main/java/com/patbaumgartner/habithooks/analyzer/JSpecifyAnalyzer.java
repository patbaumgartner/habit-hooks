package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks whether JSpecify is present and meaningfully adopted in main sources.
 */
public final class JSpecifyAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSpecifyAnalyzer.class);

    private static final String TOOL_PREFIX = "jspecify";

    private static final String POM = "pom.xml";

    private static final String SOURCE_ROOT = "src/main/java";

    @Override
    public String toolPrefix() {
        return TOOL_PREFIX;
    }

    @Override
    public boolean requiresFiles() {
        return false;
    }

    @Override
    public boolean isAvailable(Path workingDir) {
        return Files.isRegularFile(workingDir.resolve(POM));
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        if (!declaresJSpecifyDependency(workingDir)) {
            return List.of(violation("DependencyMissing", POM,
                    "JSpecify analyzer is enabled, but pom.xml does not " + "declare org.jspecify:jspecify."));
        }
        if (usesJSpecifyAnnotations(workingDir)) {
            return List.of();
        }
        return List.of(violation("NotAdopted", SOURCE_ROOT,
                "JSpecify is on the classpath, but main sources do not use @NullMarked, @Nullable, or @NonNull."));
    }

    private static boolean declaresJSpecifyDependency(Path workingDir) {
        try {
            String pom = Files.readString(workingDir.resolve(POM));
            return pom.contains("org.jspecify") && pom.contains("jspecify");
        }
        catch (IOException ex) {
            LOGGER.error("Could not read {}: {}", POM, ex.getMessage(), ex);
            return false;
        }
    }

    private static boolean usesJSpecifyAnnotations(Path workingDir) {
        Path sourceRoot = workingDir.resolve(SOURCE_ROOT);
        if (!Files.isDirectory(sourceRoot)) {
            return false;
        }
        try (Stream<Path> sources = Files.walk(sourceRoot)) {
            return sources.filter(path -> path.toString().endsWith(".java")).anyMatch(JSpecifyAnalyzer::containsMarker);
        }
        catch (IOException ex) {
            LOGGER.error("Could not inspect JSpecify source usage: {}", ex.getMessage(), ex);
            return false;
        }
    }

    private static boolean containsMarker(Path source) {
        try {
            String content = Files.readString(source);
            return content.contains("org.jspecify.annotations") || content.contains("@NullMarked")
                    || content.contains("@Nullable") || content.contains("@NonNull");
        }
        catch (IOException ex) {
            LOGGER.error("Could not read {}: {}", source, ex.getMessage(), ex);
            return false;
        }
    }

    private static Violation violation(String rule, String file, String message) {
        return new Violation(TOOL_PREFIX + ":" + rule, file, 1, message);
    }

}
