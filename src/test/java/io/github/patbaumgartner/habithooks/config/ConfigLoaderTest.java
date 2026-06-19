package io.github.patbaumgartner.habithooks.config;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void returnsDefaultConfigWhenNoFileExists() {
        HabitHooksConfig config = ConfigLoader.load(null, tempDir);
        assertThat(config).isNotNull();
        assertThat(config.getScope().getBranchBase()).isEqualTo("main");
        assertThat(config.getScope().isOnlyChangedFiles()).isTrue();
    }

    @Test
    void loadsConfigFromFile() throws Exception {
        Path configFile = tempDir.resolve(".habit-hooks.yaml");
        java.nio.file.Files.writeString(configFile, """
                prompts: ./custom-prompts
                scope:
                  onlyChangedFiles: false
                  branchBase: develop
                """);
        HabitHooksConfig config = ConfigLoader.load(null, tempDir);
        assertThat(config.getPromptsDir()).isEqualTo("./custom-prompts");
        assertThat(config.getScope().getBranchBase()).isEqualTo("develop");
        assertThat(config.getScope().isOnlyChangedFiles()).isFalse();
    }

    @Test
    void loadsConfigFromExplicitPath() throws Exception {
        Path configFile = tempDir.resolve("custom.yaml");
        java.nio.file.Files.writeString(configFile, """
                prompts: ./prompts-custom
                """);
        HabitHooksConfig config = ConfigLoader.load(configFile.toString(), tempDir);
        assertThat(config.getPromptsDir()).isEqualTo("./prompts-custom");
    }

    @Test
    void returnsDefaultConfigOnMalformedYaml() throws Exception {
        Path configFile = tempDir.resolve(".habit-hooks.yaml");
        java.nio.file.Files.writeString(configFile, "{ invalid yaml: [");
        HabitHooksConfig config = ConfigLoader.load(null, tempDir);
        assertThat(config).isNotNull();
        assertThat(config.getScope().getBranchBase()).isEqualTo("main");
    }
}
