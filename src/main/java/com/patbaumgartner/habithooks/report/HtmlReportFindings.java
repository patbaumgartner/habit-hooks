package com.patbaumgartner.habithooks.report;

import java.util.List;

/** Renders the finding list for the HTML quality report. */
final class HtmlReportFindings {

    private static final int FINDING_LIMIT = 100;

    private HtmlReportFindings() {
    }

    static String render(List<ReportFinding> findings) {
        StringBuilder output = new StringBuilder("<section class=\"panel\"><h2>Agent task feed</h2>");
        if (findings.isEmpty()) {
            return output.append("<p class=\"empty\">No findings.</p></section>").toString();
        }
        output.append("<div class=\"findings\">");
        findings.stream().limit(FINDING_LIMIT).forEach(finding -> appendFinding(output, finding));
        output.append("</div>");
        return appendFindingLimit(output, findings.size()).append("</section>").toString();
    }

    private static void appendFinding(StringBuilder output, ReportFinding finding) {
        output.append("<article class=\"finding ")
            .append(HtmlEscaper.escape(finding.severity()))
            .append("\"><div class=\"finding-head\"><span class=\"badge ")
            .append(HtmlEscaper.escape(finding.severity()))
            .append("\">")
            .append(HtmlEscaper.escape(finding.severity()))
            .append("</span><code>")
            .append(HtmlEscaper.escape(finding.ruleId()))
            .append("</code><span class=\"meta\">")
            .append(HtmlEscaper.escape(finding.dimension()))
            .append(" / ")
            .append(HtmlEscaper.escape(location(finding)))
            .append("</span></div><p class=\"message\">")
            .append(HtmlEscaper.escape(finding.message()))
            .append("</p></article>");
    }

    private static StringBuilder appendFindingLimit(StringBuilder output, int count) {
        if (count > FINDING_LIMIT) {
            output.append("<p class=\"limit\">Only the first ")
                .append(FINDING_LIMIT)
                .append(" findings are shown.</p>");
        }
        return output;
    }

    private static String location(ReportFinding finding) {
        return finding.line() > 0 ? finding.file() + ":" + finding.line() : finding.file();
    }

}
