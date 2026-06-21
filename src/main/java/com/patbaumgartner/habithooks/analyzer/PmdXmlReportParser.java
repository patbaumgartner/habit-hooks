package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses Maven PMD XML reports. */
final class PmdXmlReportParser {

    private PmdXmlReportParser() {
    }

    static List<Violation> parse(Path reportPath, Path workingDir, String toolPrefix)
            throws ParserConfigurationException, IOException, SAXException {
        NodeList files = ReportSupport.parseXml(reportPath).getElementsByTagName("file");
        List<Violation> violations = new ArrayList<>();
        for (int fileIndex = 0; fileIndex < files.getLength(); fileIndex++) {
            collectFileViolations((Element) files.item(fileIndex), workingDir, toolPrefix, violations);
        }
        return List.copyOf(violations);
    }

    private static void collectFileViolations(Element file, Path workingDir, String toolPrefix,
            List<Violation> violations) {
        String fileName = ReportSupport.relativize(Path.of(file.getAttribute("name")), workingDir);
        NodeList nodes = file.getElementsByTagName("violation");
        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            violations.add(toViolation((Element) nodes.item(nodeIndex), fileName, toolPrefix));
        }
    }

    private static Violation toViolation(Element violation, String fileName, String toolPrefix) {
        String rule = valueOrDefault(violation.getAttribute("rule"), "Violation");
        int line = ReportSupport.parseInt(violation.getAttribute("beginline"));
        String message = valueOrDefault(violation.getTextContent().strip(), rule);
        return new Violation(toolPrefix + ":" + rule, fileName, line, message);
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

}
