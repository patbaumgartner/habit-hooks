package com.patbaumgartner.habithooks.analyzer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

final class MavenOutputSummary {

    private static final List<String> MAVEN_FOOTER_LINES = List.of("[Help ",
            "cwiki.apache.org/confluence/display/MAVEN", "To see the full stack trace",
            "Re-run Maven using the -X switch", "For more information about the errors", "BUILD FAILURE",
            "BUILD SUCCESS", "Finished at:", "Total time:");

    private MavenOutputSummary() {
    }

    static String summarize(String output, Path workingDir, Optional<Path> outputLog) {
        String summary = output.lines()
                .map(String::strip)
                .filter(MavenOutputSummary::isUsefulOutputLine)
                .reduce((previous, current) -> current)
                .orElse("");
        String logMessage = outputLog.map(path -> " See " + ReportSupport.relativize(path, workingDir) + ".")
                .orElse("");
        return summary.isBlank() ? logMessage : " Last Maven output: " + summary + logMessage;
    }

    private static boolean isUsefulOutputLine(String line) {
        return !line.isBlank() && MAVEN_FOOTER_LINES.stream().noneMatch(line::contains);
    }

}
