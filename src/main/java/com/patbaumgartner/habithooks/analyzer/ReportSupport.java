package com.patbaumgartner.habithooks.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Shared helpers for report parser implementations. */
final class ReportSupport {

    private ReportSupport() {
    }

    static Document parseXml(Path reportPath) throws ParserConfigurationException, IOException, SAXException {
        return createDocumentBuilder().parse(reportPath.toFile());
    }

    static Optional<String> textOfFirst(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return Optional.empty();
        }
        String text = nodes.item(0).getTextContent().strip();
        return text.isBlank() ? Optional.empty() : Optional.of(text);
    }

    static String reportFilePath(Path reportPath, Path workingDir) {
        return relativize(reportPath.toAbsolutePath(), workingDir);
    }

    static int parseInt(String value) {
        try {
            return Integer.parseInt(value.strip());
        }
        catch (NumberFormatException ex) {
            return 1;
        }
    }

    static String relativize(Path path, Path workingDir) {
        if (!path.isAbsolute()) {
            return path.toString();
        }
        try {
            return workingDir.toAbsolutePath().relativize(path).toString();
        }
        catch (IllegalArgumentException ex) {
            return path.toString();
        }
    }

    private static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        return builder;
    }

}
