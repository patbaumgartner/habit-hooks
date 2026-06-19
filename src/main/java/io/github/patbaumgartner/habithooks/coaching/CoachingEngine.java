package io.github.patbaumgartner.habithooks.coaching;

import io.github.patbaumgartner.habithooks.model.AnalysisResult;
import io.github.patbaumgartner.habithooks.model.CoachingGroup;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Transforms an {@link AnalysisResult} into a list of {@link CoachingGroup} objects,
 * grouping violations by rule ID and attaching coaching prompts.
 */
public final class CoachingEngine {

    private final PromptLoader promptLoader;

    /**
     * Creates a coaching engine backed by the given prompt loader.
     *
     * @param promptLoader the prompt source for coaching text
     */
    public CoachingEngine(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    /**
     * Groups violations by rule and enriches each group with a coaching prompt.
     *
     * @param result the raw analysis result to coach
     * @return an ordered list of coaching groups (coached rules first, then uncoached)
     */
    public List<CoachingGroup> coach(AnalysisResult result) {
        Map<String, List<Violation>> byRule = groupByRule(result.violations());
        List<CoachingGroup> coached = new ArrayList<>();
        List<CoachingGroup> uncoached = new ArrayList<>();

        for (Map.Entry<String, List<Violation>> entry : byRule.entrySet()) {
            CoachingGroup group = buildGroup(entry.getKey(), entry.getValue());
            if (group.isCoached()) {
                coached.add(group);
            } else {
                uncoached.add(group);
            }
        }

        List<CoachingGroup> all = new ArrayList<>(coached);
        all.addAll(uncoached);
        return List.copyOf(all);
    }

    private static Map<String, List<Violation>> groupByRule(List<Violation> violations) {
        Map<String, List<Violation>> byRule = new LinkedHashMap<>();
        for (Violation v : violations) {
            byRule.computeIfAbsent(v.ruleId(), k -> new ArrayList<>()).add(v);
        }
        return byRule;
    }

    private CoachingGroup buildGroup(String ruleId, List<Violation> violations) {
        String title = RuleTitles.titleFor(ruleId);
        Optional<String> coaching = promptLoader.load(ruleId);
        return new CoachingGroup(ruleId, title, coaching, violations);
    }
}
