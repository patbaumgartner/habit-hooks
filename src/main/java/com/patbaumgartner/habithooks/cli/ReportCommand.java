package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.report.QualityReport;
import com.patbaumgartner.habithooks.report.QualityReportBuilder;
import com.patbaumgartner.habithooks.report.QualityReportWriter;
import com.patbaumgartner.habithooks.report.ReportFormat;
import com.patbaumgartner.habithooks.report.TrendStore;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

/** Generates local Sonar-style quality reports. */
@Command(name = "report", mixinStandardHelpOptions = true,
        description = "Write a local quality report for agents and humans.")
final class ReportCommand implements Callable<Integer> {

    @ParentCommand
    private HabitHooksCommand parent;

    @Option(names = { "--format" }, description = "markdown, md, json, html, or sarif", defaultValue = "markdown",
            paramLabel = "<format>")
    private String format;

    @Option(names = { "--output" }, description = "Output directory, relative to the project root",
            defaultValue = "target/habit-hooks", paramLabel = "<dir>")
    private Path outputDir;

    @Option(names = { "--no-fail" }, description = "Always exit 0 after writing the report")
    private boolean noFail;

    @Override
    public Integer call() throws Exception {
        ReportFormat normalizedFormat = parseFormat();
        if (normalizedFormat == null) {
            return 2;
        }
        Path workingDir = parent.workingDir();
        Path resolvedOutputDir = resolveOutputDir(workingDir);
        AnalysisRun run = parent.analyzeConfigured(workingDir);
        if (run.skipped()) {
            System.out.println(run.skipMessage());
            return 0;
        }
        QualityReport report = new QualityReportBuilder().build(run.result(), run.hasFailures());
        Optional<TrendStore.Snapshot> previous = new TrendStore().record(resolvedOutputDir.resolve("history"), report);
        Path output = new QualityReportWriter().write(report, resolvedOutputDir, normalizedFormat, previous);
        System.out.println("Wrote " + output);
        previous.ifPresent(snapshot -> System.out.println(trendLine(report, snapshot)));
        return noFail || !run.hasFailures() ? 0 : 1;
    }

    private ReportFormat parseFormat() {
        try {
            return ReportFormat.parse(format);
        }
        catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return null;
        }
    }

    private Path resolveOutputDir(Path workingDir) {
        return outputDir.isAbsolute() ? outputDir : workingDir.resolve(outputDir);
    }

    private static String trendLine(QualityReport report, TrendStore.Snapshot previous) {
        int delta = report.totalFindings() - previous.totalFindings();
        String formattedDelta = delta > 0 ? "+" + delta : Integer.toString(delta);
        return "Trend: " + formattedDelta + " findings since " + previous.generatedAt();
    }

}
