package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Parses CPD duplication XML reports into {@code pmd:CopyPaste} violations. */
final class CpdXmlReportParser {

    private static final String COPY_PASTE_RULE = "pmd:CopyPaste";

    private CpdXmlReportParser() {
    }

    static List<Violation> parse(Path reportPath, Path workingDir, String toolPrefix)
            throws ParserConfigurationException, IOException, SAXException {
        NodeList duplications = ReportSupport.parseXml(reportPath).getElementsByTagName("duplication");
        List<Violation> violations = new ArrayList<>();
        for (int index = 0; index < duplications.getLength(); index++) {
            toViolation((Element) duplications.item(index), workingDir).ifPresent(violations::add);
        }
        return List.copyOf(violations);
    }

    private static Optional<Violation> toViolation(Element duplication, Path workingDir) {
        NodeList files = duplication.getElementsByTagName("file");
        if (files.getLength() == 0) {
            return Optional.empty();
        }
        Element first = (Element) files.item(0);
        String fileName = ReportSupport.relativize(Path.of(first.getAttribute("path")), workingDir);
        int line = ReportSupport.parseInt(first.getAttribute("line"));
        return Optional.of(new Violation(COPY_PASTE_RULE, fileName, line, message(duplication, files, workingDir)));
    }

    private static String message(Element duplication, NodeList files, Path workingDir) {
        return "Duplicated block of " + duplication.getAttribute("lines") + " lines ("
                + duplication.getAttribute("tokens") + " tokens), also found at " + otherLocations(files, workingDir)
                + ".";
    }

    private static String otherLocations(NodeList files, Path workingDir) {
        StringJoiner joiner = new StringJoiner(", ");
        for (int index = 1; index < files.getLength(); index++) {
            Element file = (Element) files.item(index);
            String name = ReportSupport.relativize(Path.of(file.getAttribute("path")), workingDir);
            joiner.add(name + ":" + file.getAttribute("line"));
        }
        return joiner.length() == 0 ? "another location" : joiner.toString();
    }

}
