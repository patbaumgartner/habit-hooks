package com.patbaumgartner.habithooks.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads {@link HabitHooksConfig} from a YAML file on disk.
 *
 * <p>
 * Config is optional — if no file is found the loader returns the default configuration
 * so habit-hooks can run without any setup.
 */
public final class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

    private static final String DEFAULT_CONFIG_FILE = ".habit-hooks.yaml";

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private ConfigLoader() {
        // utility class
    }

    /**
     * Loads config from the given path, falling back to the default config when the file
     * does not exist.
     * @param configPath explicit config path, or {@code null} to use the default
     * @param workingDir the project root directory
     * @return the loaded (or default) configuration
     */
    public static HabitHooksConfig load(String configPath, Path workingDir) {
        Path target = resolveConfigPath(configPath, workingDir);
        return readConfig(target).orElseGet(() -> {
            LOGGER.debug("No config file found at {}; using defaults.", target);
            return new HabitHooksConfig();
        });
    }

    private static Path resolveConfigPath(String configPath, Path workingDir) {
        if (configPath != null && !configPath.isBlank()) {
            Path explicitPath = Path.of(configPath);
            if (explicitPath.isAbsolute()) {
                return explicitPath;
            }
            return workingDir.resolve(explicitPath);
        }
        return workingDir.resolve(DEFAULT_CONFIG_FILE);
    }

    private static Optional<HabitHooksConfig> readConfig(Path path) {
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(path)) {
            HabitHooksConfig config = YAML_MAPPER.readValue(in, HabitHooksConfig.class);
            LOGGER.debug("Loaded config from {}", path);
            return Optional.of(config);
        }
        catch (IOException ex) {
            LOGGER.warn("Failed to parse config file {}: {}. Using defaults.", path, ex.getMessage());
            return Optional.empty();
        }
    }

}
