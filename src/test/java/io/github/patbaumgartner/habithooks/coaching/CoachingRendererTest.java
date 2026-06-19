package io.github.patbaumgartner.habithooks.coaching;

import io.github.patbaumgartner.habithooks.model.CoachingGroup;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoachingRendererTest {

    @Test
    void rendersPassWhenNoViolations() {
        String output = render(List.of());
        assertThat(output).contains("✅ Habit Hooks: all checks passed.");
    }

    @Test
    void rendersFailHeaderWithViolationCount() {
        CoachingGroup group = new CoachingGroup(
                "checkstyle:MethodLength",
                "Oversized Method",
                Optional.of("Methods over 25 lines…"),
                List.of(new Violation("checkstyle:MethodLength", "Foo.java", 5, "Too long")));
        String output = render(List.of(group));
        assertThat(output).contains("❌ Habit Hooks: 1 violation");
    }

    @Test
    void rendersCoachedGroupWithPrompt() {
        CoachingGroup group = new CoachingGroup(
                "checkstyle:MethodLength",
                "Oversized Method",
                Optional.of("Coaching text here."),
                List.of(new Violation("checkstyle:MethodLength", "Bar.java", 10, "Too long")));
        String output = render(List.of(group));
        assertThat(output)
                .contains("❌ Oversized Method")
                .contains("Coaching text here.")
                .contains("Violations:")
                .contains("Bar.java:10");
    }

    @Test
    void rendersUncoachedSectionForRulesWithNoPrompt() {
        CoachingGroup uncoached = new CoachingGroup(
                "custom:WeirdRule",
                "custom:WeirdRule",
                Optional.empty(),
                List.of(new Violation("custom:WeirdRule", "Baz.java", 3, "weird")));
        String output = render(List.of(uncoached));
        assertThat(output)
                .contains("⚠️  Uncoached rules")
                .contains("custom:WeirdRule");
    }

    @Test
    void rendersTaikaiHintOnCleanRun() {
        String output = render(List.of());
        assertThat(output).contains("Taikai");
    }

    private static String render(List<CoachingGroup> groups) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CoachingRenderer(new PrintStream(baos, true, StandardCharsets.UTF_8))
                .render(groups);
        return baos.toString(StandardCharsets.UTF_8);
    }
}
