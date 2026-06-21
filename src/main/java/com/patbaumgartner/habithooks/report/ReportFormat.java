package com.patbaumgartner.habithooks.report;

import java.util.Locale;
import java.util.Map;

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

    private static final Map<String, ReportFormat> FORMATS = Map.of("markdown", MARKDOWN, "md", MARKDOWN, "json", JSON,
            "html", HTML, "sarif", SARIF);

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
        ReportFormat format = FORMATS.get(normalized);
        if (format != null) {
            return format;
        }
        throw new IllegalArgumentException(
                "Unsupported report format '" + value + "'. Use one of: markdown, md, json, html, sarif.");
    }

}
