package io.github.patbaumgartner.habithooks.coaching;

import io.github.patbaumgartner.habithooks.model.AnalysisResult;
import io.github.patbaumgartner.habithooks.model.CoachingGroup;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoachingEngineTest {

    private final CoachingEngine engine =
            new CoachingEngine(new PromptLoader(Path.of("/nonexistent")));

    @Test
    void returnsEmptyWhenNoViolations() {
        AnalysisResult result = new AnalysisResult(List.of(), 5);
        assertThat(engine.coach(result)).isEmpty();
    }

    @Test
    void groupsViolationsByRule() {
        List<Violation> violations = List.of(
                new Violation("checkstyle:MethodLength", "A.java", 1, "Too long"),
                new Violation("checkstyle:MethodLength", "B.java", 2, "Too long"),
                new Violation("pmd:GodClass",            "C.java", 1, "God class")
        );
        AnalysisResult result = new AnalysisResult(violations, 3);
        List<CoachingGroup> groups = engine.coach(result);

        assertThat(groups).hasSize(2);
        CoachingGroup methodLength = groups.stream()
                .filter(g -> g.ruleId().equals("checkstyle:MethodLength"))
                .findFirst().orElseThrow();
        assertThat(methodLength.violations()).hasSize(2);
    }

    @Test
    void coachedGroupsAppearBeforeUncoached() {
        List<Violation> violations = List.of(
                new Violation("custom:UnknownRule",      "X.java", 1, "unknown"),
                new Violation("checkstyle:MethodLength", "Y.java", 5, "too long")
        );
        AnalysisResult result = new AnalysisResult(violations, 2);
        List<CoachingGroup> groups = engine.coach(result);

        assertThat(groups.get(0).ruleId()).isEqualTo("checkstyle:MethodLength");
        assertThat(groups.get(1).ruleId()).isEqualTo("custom:UnknownRule");
    }

    @Test
    void titlesAreAssignedToKnownRules() {
        List<Violation> violations =
                List.of(new Violation("pmd:GodClass", "Big.java", 1, "God class"));
        AnalysisResult result = new AnalysisResult(violations, 1);
        List<CoachingGroup> groups = engine.coach(result);

        assertThat(groups.get(0).title()).isEqualTo("God Class");
    }

    @Test
    void unknownRuleUsesRuleIdAsTitle() {
        List<Violation> violations =
                List.of(new Violation("custom:MyRule", "Foo.java", 1, "msg"));
        AnalysisResult result = new AnalysisResult(violations, 1);
        List<CoachingGroup> groups = engine.coach(result);

        assertThat(groups.get(0).title()).isEqualTo("custom:MyRule");
    }
}
