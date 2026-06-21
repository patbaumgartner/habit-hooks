package com.patbaumgartner.habithooks.report;

/** Escapes text for HTML text nodes and attribute-safe class fragments. */
final class HtmlEscaper {

    private HtmlEscaper() {
    }

    static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
