package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses JaCoCo XML reports. */
final class JacocoReportParser {

    private static final int PERCENT = 100;

    private JacocoReportParser() {
    }

    static List<Violation> parse(Path reportPath, Path workingDir, String toolPrefix)
            throws ParserConfigurationException, IOException, SAXException {
        Optional<Coverage> coverage = lineCoverage(ReportSupport.parseXml(reportPath));
        if (coverage.isEmpty() || coverage.get().missed() == 0) {
            return List.of();
        }
        return List.of(toViolation(reportPath, workingDir, toolPrefix, coverage.get()));
    }

    private static Violation toViolation(Path reportPath, Path workingDir, String toolPrefix, Coverage coverage) {
        int total = coverage.missed() + coverage.covered();
        int percentage = coverage.covered() * PERCENT / total;
        String message = "Line coverage is " + percentage + "% (" + coverage.covered() + "/" + total
                + " lines covered).";
        return new Violation(toolPrefix + ":LineCoverage", ReportSupport.reportFilePath(reportPath, workingDir), 1,
                message);
    }

    private static Optional<Coverage> lineCoverage(Document document) {
        NodeList counters = document.getElementsByTagName("counter");
        for (int index = 0; index < counters.getLength(); index++) {
            Element counter = (Element) counters.item(index);
            if ("LINE".equals(counter.getAttribute("type"))) {
                return Optional.of(new Coverage(ReportSupport.parseInt(counter.getAttribute("missed")),
                        ReportSupport.parseInt(counter.getAttribute("covered"))));
            }
        }
        return Optional.empty();
    }

    private record Coverage(int missed, int covered) {
    }

}
