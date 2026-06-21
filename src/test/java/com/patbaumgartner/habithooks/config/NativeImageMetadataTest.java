package com.patbaumgartner.habithooks.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NativeImageMetadataTest {

    private static final Path REFLECT_CONFIG = Path
        .of("src/main/resources/META-INF/native-image/com.patbaumgartner/habit-hooks/reflect-config.json");

    @Test
    void registersConfigTypesForJacksonReflection() throws Exception {
        String metadata = Files.readString(REFLECT_CONFIG);

        for (Class<?> configType : configTypes()) {
            assertThat(metadata).contains("\"name\": \"" + configType.getName() + "\"");
        }
        assertThat(metadata).contains("\"allDeclaredConstructors\": true", "\"allDeclaredFields\": true",
                "\"allDeclaredMethods\": true");
    }

    private static List<Class<?>> configTypes() {
        return List.of(HabitHooksConfig.class, ScopeConfig.class, AnalyzerConfig.class, RuleConfig.class);
    }

}