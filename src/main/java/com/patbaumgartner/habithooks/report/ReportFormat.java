package com.patbaumgartner.habithooks.report;

import java.util.Locale;

/** Supported quality report formats. */
public enum ReportFormat {

    /** Markdown report, written as {@code report.md}. */
    MARKDOWN("md"),

    /** JSON report, written as {@code report.json}. */
    JSON("json"),

    /** Static HTML report, written as {@code report.html}. */
    HTML("html"),

    /** SARIF report, written as {@code report.sarif}. */
    SARIF("sarif");

    private final String extension;

    ReportFormat(String extension) {
        this.extension = extension;
    }

    /** Returns the file extension for this format. */
    public String extension() {
        return extension;
    }

    /** Parses a user-supplied format name. */
    public static ReportFormat parse(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "markdown", "md" -> MARKDOWN;
            case "json" -> JSON;
            case "html" -> HTML;
            case "sarif" -> SARIF;
            default -> throw new IllegalArgumentException(
                    "Unsupported report format '" + value + "'. Use one of: markdown, md, json, html, sarif.");
        };
    }

}
