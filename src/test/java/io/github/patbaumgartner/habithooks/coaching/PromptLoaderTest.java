package io.github.patbaumgartner.habithooks.coaching;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptLoaderTest {

    @Test
    void toFilenameReplacesColonWithDash() {
        assertThat(PromptLoader.toFilename("checkstyle:MethodLength"))
                .isEqualTo("checkstyle-MethodLength.md");
    }

    @Test
    void toFilenameHandlesPmdRuleId() {
        assertThat(PromptLoader.toFilename("pmd:GodClass"))
                .isEqualTo("pmd-GodClass.md");
    }

    @Test
    void toFilenameStripsAtSign() {
        assertThat(PromptLoader.toFilename("@plugin/some:rule"))
                .isEqualTo("plugin-some-rule.md");
    }

    @Test
    void loadsBuiltInCheckstyleMethodLengthPrompt() {
        PromptLoader loader = new PromptLoader(java.nio.file.Path.of("/nonexistent"));
        java.util.Optional<String> prompt = loader.load("checkstyle:MethodLength");
        assertThat(prompt).isPresent();
        assertThat(prompt.get()).contains("responsibilities");
    }

    @Test
    void loadsBuiltInPmdGodClassPrompt() {
        PromptLoader loader = new PromptLoader(java.nio.file.Path.of("/nonexistent"));
        java.util.Optional<String> prompt = loader.load("pmd:GodClass");
        assertThat(prompt).isPresent();
        assertThat(prompt.get()).contains("Single Responsibility");
    }

    @Test
    void returnsEmptyForUnknownRule() {
        PromptLoader loader = new PromptLoader(java.nio.file.Path.of("/nonexistent"));
        assertThat(loader.load("custom:unknown:rule")).isEmpty();
    }
}
