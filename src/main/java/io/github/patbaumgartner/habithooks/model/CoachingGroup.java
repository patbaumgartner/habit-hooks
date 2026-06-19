package io.github.patbaumgartner.habithooks.model;

import java.util.List;
import java.util.Optional;

/**
 * A coached violation group: the smell title, optional coaching text, and the
 * list of individual violations that triggered it.
 *
 * @param ruleId     the rule identifier (e.g. {@code checkstyle:MethodLength})
 * @param title      short human-readable title (e.g. "Oversized Method")
 * @param coaching   optional coaching prompt explaining the smell and how to
 *                   fix it
 * @param violations the violations belonging to this group
 */
public record CoachingGroup(
        String ruleId,
        String title,
        Optional<String> coaching,
        List<Violation> violations) {

    /** Constructs a CoachingGroup with a defensive copy of the violations list. */
    public CoachingGroup {
        if (ruleId == null || ruleId.isBlank()) {
            throw new IllegalArgumentException("ruleId must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        violations = List.copyOf(violations);
        coaching = coaching == null ? Optional.empty() : coaching;
    }

    /** Returns {@code true} when a coaching prompt is available for this rule. */
    public boolean isCoached() {
        return coaching.isPresent();
    }
}
