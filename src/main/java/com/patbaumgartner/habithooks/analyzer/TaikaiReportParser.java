package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

final class TaikaiReportParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaikaiReportParser.class);

    private static final String TOOL_PREFIX = "taikai";

    private static final String SUREFIRE_DIR = "target/surefire-reports";

    private final String testClass;

    TaikaiReportParser(String testClass) {
        this.testClass = testClass;
    }

    List<Violation> parse(Path workingDir, String testFile) {
        Path surefireDir = workingDir.resolve(SUREFIRE_DIR);
        if (!Files.isDirectory(surefireDir)) {
            LOGGER.debug("No Surefire reports directory found at {}", surefireDir);
            return List.of();
        }
        List<Path> reports = findReports(surefireDir);
        return reports.isEmpty() ? List.of() : parseReports(reports, testFile);
    }

    private List<Path> findReports(Path surefireDir) {
        try (var stream = Files.list(surefireDir)) {
            return stream.filter(p -> p.getFileName().toString().startsWith("TEST-")
                    && p.getFileName().toString().endsWith(".xml") && p.getFileName().toString().contains(testClass))
                .toList();
        }
        catch (IOException e) {
            LOGGER.error("Could not list Surefire reports: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<Violation> parseReports(List<Path> reports, String testFile) {
        try {
            DocumentBuilder builder = createDocumentBuilder();
            List<Violation> violations = new ArrayList<>();
            for (Path report : reports) {
                violations.addAll(parseReport(report, builder, testFile));
            }
            return List.copyOf(violations);
        }
        catch (ParserConfigurationException e) {
            LOGGER.error("Failed to configure XML parser: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private List<Violation> parseReport(Path reportFile, DocumentBuilder builder, String testFile) {
        try {
            builder.reset();
            Document doc = builder.parse(reportFile.toFile());
            return extractViolations(doc, testFile);
        }
        catch (SAXException | IOException e) {
            LOGGER.error("Failed to parse Surefire report {}: {}", reportFile, e.getMessage(), e);
            return List.of();
        }
    }

    private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private static List<Violation> extractViolations(Document doc, String testFile) {
        List<Violation> violations = new ArrayList<>();
        NodeList testcases = doc.getElementsByTagName("testcase");
        for (int i = 0; i < testcases.getLength(); i++) {
            violations.addAll(violationsFor((Element) testcases.item(i), testFile));
        }
        return violations;
    }

    private static List<Violation> violationsFor(Element testcase, String testFile) {
        String methodName = testcase.getAttribute("name");
        NodeList failures = testcase.getElementsByTagName("failure");
        NodeList errors = testcase.getElementsByTagName("error");
        if (failures.getLength() > 0) {
            return List.of(toViolation(methodName, (Element) failures.item(0), testFile));
        }
        if (errors.getLength() > 0) {
            return List.of(toViolation(methodName, (Element) errors.item(0), testFile));
        }
        return List.of();
    }

    private static Violation toViolation(String methodName, Element element, String testFile) {
        String ruleId = TOOL_PREFIX + ":" + methodName;
        String rawMessage = element.getAttribute("message");
        String message = rawMessage.isBlank() ? element.getTextContent().strip() : rawMessage.strip();
        String firstLine = message.lines().findFirst().orElse(message);
        return new Violation(ruleId, testFile, 1, firstLine);
    }

}
