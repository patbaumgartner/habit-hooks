package io.github.patbaumgartner.habithooks.model;

/**
 * A single static analysis violation reported by an analyzer.
 *
 * @param ruleId  the fully-qualified rule identifier (e.g.
 *                {@code checkstyle:MethodLength})
 * @param file    workspace-relative path to the file containing the violation
 * @param line    1-based line number, or {@code -1} when not available
 * @param message the raw violation message from the underlying tool
 */
public record Violation(
        String ruleId,
        String file,
        int line,
        String message) {

    /**
     * Constructs a Violation and validates that required fields are present.
     */
    public Violation {
        if (ruleId == null || ruleId.isBlank()) {
            throw new IllegalArgumentException("ruleId must not be blank");
        }
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("file must not be blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    /**
     * Returns a human-readable location string, e.g. {@code UserService.java:42}.
     */
    public String location() {
        return line > 0 ? file + ":" + line : file;
    }
}
