package com.patbaumgartner.habithooks.report;

import java.util.Map;
import java.util.Optional;

/** Renders the local quality report as a self-contained HTML document. */
final class HtmlReportRenderer {

    private static final int MINIMUM_VISIBLE_BAR_PERCENT = 3;

    private static final double PERCENT_SCALE = 100.0d;

    private static final String STYLES = """
            :root{color-scheme:light;--ink:#19212a;--muted:#637083;--line:#d8dee8;--surface:#ffffff;
            --page:#f6f7f9;--accent:#0f766e;--danger:#b42318;--danger-soft:#fee4e2;--warn:#b54708;
            --warn-soft:#fff2cc;--low:#475467;--low-soft:#eef2f7}*{box-sizing:border-box}
            body{margin:0;background:var(--page);color:var(--ink);font-family:Inter,ui-sans-serif,system-ui,
            -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;line-height:1.5}.page{max-width:1180px;
            margin:0 auto;padding:32px 24px 48px}.hero{border-bottom:1px solid var(--line);padding:28px 0 24px}
            .eyebrow{color:var(--accent);font-size:.78rem;font-weight:700;text-transform:uppercase}.hero h1{
            font-size:2rem;line-height:1.15;margin:.4rem 0}.hero p{color:var(--muted);margin:0}.summary{display:grid;
            gap:12px;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));margin:24px 0}.metric,.panel{
            background:var(--surface);border:1px solid var(--line);border-radius:8px}.metric{padding:16px}.metric span{
            color:var(--muted);display:block;font-size:.82rem}.metric strong{display:block;font-size:1.6rem;line-height:1.2}
            .grid{display:grid;gap:16px;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));margin:0 0 18px}
            .panel{padding:18px}.panel h2{font-size:1rem;margin:0 0 12px}.bar{display:grid;
            grid-template-columns:minmax(7rem,1fr) minmax(2rem,auto);gap:8px;align-items:center;margin:10px 0}.track{
            height:9px;background:#edf0f5;border-radius:999px;overflow:hidden}.fill{height:100%;
            background:linear-gradient(90deg,var(--accent),#7c3aed)}.findings{display:grid;gap:10px}.finding{
            background:var(--surface);border:1px solid var(--line);border-left:4px solid var(--low);border-radius:8px;
            padding:14px 16px}.finding.critical,.finding.high{border-left-color:var(--danger)}.finding.medium{
            border-left-color:var(--warn)}.finding.low{border-left-color:var(--low)}.finding-head{display:flex;
            align-items:center;gap:10px;flex-wrap:wrap}.badge{border-radius:999px;font-size:.72rem;font-weight:700;
            padding:2px 8px;text-transform:uppercase}.badge.critical,.badge.high{background:var(--danger-soft);
            color:var(--danger)}.badge.medium{background:var(--warn-soft);color:var(--warn)}.badge.low{
            background:var(--low-soft);color:var(--low)}code{background:#f0f3f7;border:1px solid #dce3ec;border-radius:6px;
            padding:.12rem .32rem}.meta{color:var(--muted);font-size:.88rem}.message{margin:.55rem 0 0}.empty{
            color:var(--muted);padding:16px 0}.limit{color:var(--muted);font-size:.9rem;margin-top:12px}
            @media (max-width:680px){.page{padding:22px 14px 36px}.hero h1{font-size:1.6rem}.finding{padding:12px}}
            """;

    private HtmlReportRenderer() {
    }

    static String render(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>habit-hooks report</title><style>" + STYLES + "</style></head><body>" + body(report, previous)
                + "</body></html>";
    }

    private static String body(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        return "<main class=\"page\">" + hero(report) + summary(report) + overview(report, previous)
                + HtmlReportFindings.render(report.findings()) + "</main>";
    }

    private static String hero(QualityReport report) {
        return "<header class=\"hero\"><div class=\"eyebrow\">habit-hooks</div>"
                + "<h1>Local quality report</h1><p>Generated " + HtmlEscaper.escape(report.generatedAt()) + ". Gate is "
                + gate(report) + ".</p></header>";
    }

    private static String summary(QualityReport report) {
        return "<section class=\"summary\" aria-label=\"Report summary\">" + metric("Findings", report.totalFindings())
                + metric("Files checked", report.filesChecked()) + metric("Gate", gate(report))
                + metric("Status", report.clean() ? "clean" : "needs work") + "</section>";
    }

    private static String overview(QualityReport report, Optional<TrendStore.Snapshot> previous) {
        return "<section class=\"grid\">" + mapPanel("By dimension", report.byDimension(), report.totalFindings())
                + mapPanel("By tool", report.byTool(), report.totalFindings()) + TrendRenderer.html(report, previous)
                + "</section>";
    }

    private static String mapPanel(String title, Map<String, Long> counts, int total) {
        StringBuilder panel = new StringBuilder("<section class=\"panel\"><h2>").append(HtmlEscaper.escape(title))
            .append("</h2>");
        if (counts.isEmpty()) {
            return panel.append("<p class=\"empty\">No findings.</p></section>").toString();
        }
        counts.forEach((key, value) -> appendBar(panel, key, value, total));
        return panel.append("</section>").toString();
    }

    private static void appendBar(StringBuilder panel, String label, long value, int total) {
        panel.append("<div class=\"bar\"><span>")
            .append(HtmlEscaper.escape(label))
            .append("</span><strong>")
            .append(value)
            .append("</strong><div class=\"track\"><div class=\"fill\" style=\"width:")
            .append(percent(value, total))
            .append("%\"></div></div></div>");
    }

    private static String metric(String label, int value) {
        return metric(label, Integer.toString(value));
    }

    private static String metric(String label, String value) {
        return "<div class=\"metric\"><span>" + HtmlEscaper.escape(label) + "</span><strong>"
                + HtmlEscaper.escape(value) + "</strong></div>";
    }

    private static long percent(long value, int total) {
        return total == 0 ? 0 : Math.max(MINIMUM_VISIBLE_BAR_PERCENT, Math.round((value * PERCENT_SCALE) / total));
    }

    private static String gate(QualityReport report) {
        return report.failing() ? "failing" : "passing";
    }

}
