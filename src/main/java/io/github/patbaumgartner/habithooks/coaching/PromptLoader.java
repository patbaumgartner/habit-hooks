package io.github.patbaumgartner.habithooks.coaching;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads coaching prompt text for a given rule ID.
 *
 * <p>Lookup order:
 * <ol>
 *   <li>User-supplied prompts directory (configured via {@code prompts:} in the YAML config)
 *   <li>Built-in classpath prompts bundled with habit-hooks
 * </ol>
 *
 * <p>Prompt file naming convention: {@code <tool>-<RuleName>.md}, where colons and
 * slashes in the rule ID are replaced with {@code -} and leading {@code @} is dropped.
 * For example, {@code checkstyle:MethodLength} → {@code checkstyle-MethodLength.md}.
 */
public final class PromptLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromptLoader.class);
    private static final String CLASSPATH_PREFIX =
            "io/github/patbaumgartner/habithooks/prompts/";

    private final Path userPromptsDir;

    /**
     * Creates a loader that will check the given user prompts directory before
     * falling back to the classpath.
     *
     * @param userPromptsDir path to the user-supplied prompts directory (may not exist)
     */
    public PromptLoader(Path userPromptsDir) {
        this.userPromptsDir = userPromptsDir;
    }

    /**
     * Loads the coaching prompt for the given rule ID.
     *
     * @param ruleId the rule ID (e.g. {@code "checkstyle:MethodLength"})
     * @return the prompt text, or {@link Optional#empty()} if no prompt is found
     */
    public Optional<String> load(String ruleId) {
        String filename = toFilename(ruleId);
        return loadFromUserDir(filename)
                .or(() -> loadFromClasspath(filename));
    }

    private Optional<String> loadFromUserDir(String filename) {
        Path candidate = userPromptsDir.resolve(filename);
        if (!Files.isRegularFile(candidate)) {
            return Optional.empty();
        }
        try {
            String content = Files.readString(candidate, StandardCharsets.UTF_8);
            LOGGER.debug("Loaded user prompt from {}", candidate);
            return Optional.of(content.strip());
        } catch (IOException e) {
            LOGGER.warn("Failed to read user prompt {}: {}", candidate, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> loadFromClasspath(String filename) {
        String resource = CLASSPATH_PREFIX + filename;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return Optional.empty();
            }
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.debug("Loaded built-in prompt from classpath:{}", resource);
            return Optional.of(content.strip());
        } catch (IOException e) {
            LOGGER.warn("Failed to read classpath prompt {}: {}", resource, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Converts a rule ID to a prompt filename.
     * {@code checkstyle:MethodLength} → {@code checkstyle-MethodLength.md}
     */
    static String toFilename(String ruleId) {
        return ruleId
                .replace(":", "-")
                .replace("/", "-")
                .replace("@", "")
                + ".md";
    }
}
