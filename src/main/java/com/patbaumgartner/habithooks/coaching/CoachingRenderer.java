package com.patbaumgartner.habithooks.coaching;

import com.patbaumgartner.habithooks.model.CoachingGroup;
import com.patbaumgartner.habithooks.model.Violation;
import java.io.PrintStream;
import java.util.List;

/**
 * Renders coaching groups to a human-readable text format on a {@link PrintStream}.
 *
 * <p>
 * The output format mirrors the TypeScript habit-hooks output so agents trained on either
 * edition will recognise the structure.
 */
public final class CoachingRenderer {

    private static final String FAIL_HEADER = "❌ Habit Hooks: %d violation%s%n%n";

    private static final String PASS_HEADER = "✅ Habit Hooks: all checks passed.%n";

    private static final String TAIKAI_HINT = "%nhabit-hooks catches structural smells, not correctness or design. "
            + "Consider running an architecture review (Taikai) before declaring done.%n";

    private static final String COACHED_TITLE = "❌ %s%n";

    private static final String UNCOACHED_HDR = "⚠️  Uncoached rules%n%n";

    private static final String UNCOACHED_RULE_HDR = "  %s (%d violation%s)%n";

    private static final String VIOLATION_FMT = "  %s%n    %s%n";

    private static final String MORE_FMT = "    … and %d more%n";

    private static final int MAX_UNCOACHED_SHOWN = 10;

    private final Output out;

    /**
     * Creates a renderer writing to the given stream.
     * @param out the output stream
     */
    public CoachingRenderer(PrintStream out) {
        this.out = out::printf;
    }

    /**
     * Renders the full coaching output for the given list of groups.
     * @param groups the coaching groups to render
     */
    public void render(List<CoachingGroup> groups) {
        int total = groups.stream().mapToInt(g -> g.violations().size()).sum();
        if (total == 0) {
            renderClean();
            return;
        }
        renderFailHeader(total);
        List<CoachingGroup> coached = coached(groups);
        List<CoachingGroup> uncoached = uncoached(groups);
        coached.forEach(this::renderCoachedGroup);
        if (!uncoached.isEmpty()) {
            renderUncoachedSection(uncoached);
        }
    }

    private void renderClean() {
        out.printf(PASS_HEADER);
        out.printf(TAIKAI_HINT);
    }

    private void renderFailHeader(int total) {
        out.printf(FAIL_HEADER, total, total == 1 ? "" : "s");
    }

    private void renderCoachedGroup(CoachingGroup group) {
        out.printf(COACHED_TITLE, group.title());
        group.coaching().ifPresent(c -> out.printf("%s%n%n", c));
        out.printf("Violations:%n");
        group.violations().forEach(v -> out.printf(VIOLATION_FMT, v.location(), v.message()));
        out.println();
    }

    private void renderUncoachedSection(List<CoachingGroup> uncoached) {
        out.printf(UNCOACHED_HDR);
        out.printf("The following rules fired but have no coaching prompt. "
                + "Add a <tool>-<RuleName>.md file to your prompts directory to coach them.%n%n");
        for (CoachingGroup group : uncoached) {
            int total = group.violations().size();
            out.printf(UNCOACHED_RULE_HDR, group.ruleId(), total, total == 1 ? "" : "s");
            List<Violation> shown = group.violations().subList(0, Math.min(MAX_UNCOACHED_SHOWN, total));
            shown.forEach(v -> out.printf(VIOLATION_FMT, v.location(), v.message()));
            int remaining = total - shown.size();
            if (remaining > 0) {
                out.printf(MORE_FMT, remaining);
            }
            out.println();
        }
    }

    private static List<CoachingGroup> coached(List<CoachingGroup> groups) {
        return groups.stream().filter(CoachingGroup::isCoached).toList();
    }

    private static List<CoachingGroup> uncoached(List<CoachingGroup> groups) {
        return groups.stream().filter(g -> !g.isCoached()).toList();
    }

    @FunctionalInterface
    private interface Output {

        void printf(String format, Object... args);

        default void println() {
            printf("%n");
        }

    }

}
