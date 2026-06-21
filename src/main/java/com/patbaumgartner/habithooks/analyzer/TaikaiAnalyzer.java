package com.patbaumgartner.habithooks.analyzer;

import com.patbaumgartner.habithooks.model.Violation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Analyzer that runs a Taikai architecture-test class via Maven and turns each test
 * failure into a {@link Violation}.
 *
 * <p>
 * Taikai tests are project-wide, not file-scoped, so the {@code files} parameter of
 * {@link #analyze} is intentionally ignored — the whole architecture is checked on every
 * run.
 *
 * <p>
 * Lookup order for the Maven wrapper:
 * <ol>
 * <li>{@code ./mvnw} in the working directory (preferred)
 * <li>{@code mvn} on {@code PATH}
 * </ol>
 *
 * <p>
 * After the run, Surefire XML reports are parsed from
 * {@code target/surefire-reports/TEST-*<testClass>*.xml}. Each {@code <failure>} or
 * {@code <error>} element in a {@code <testcase>} becomes a violation with rule ID
 * {@code taikai:<testMethodName>}.
 */
public non-sealed class TaikaiAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaikaiAnalyzer.class);

    private static final String TOOL_PREFIX = "taikai";

    private static final String SUREFIRE_DIR = "target/surefire-reports";

    private final String testClass;

    /**
     * Creates an analyzer that invokes the given test class.
     * @param testClass simple name of the JUnit test class (e.g.
     * {@code "ArchitectureTest"})
     */
    public TaikaiAnalyzer(String testClass) {
        this.testClass = testClass;
    }

    @Override
    public String toolPrefix() {
        return TOOL_PREFIX;
    }

    /**
     * Returns {@code true} when a Maven wrapper or {@code mvn} command is present
     * <em>and</em> the test class exists somewhere under {@code src/test/java}.
     */
    @Override
    public boolean isAvailable(Path workingDir) {
        boolean hasMaven = Files.isRegularFile(workingDir.resolve("mvnw"))
                || Files.isRegularFile(workingDir.resolve("mvnw.cmd")) || commandExists("mvn");
        if (!hasMaven) {
            return false;
        }
        return findTestClass(workingDir).map(testFile -> hasRequiredTaikaiDependency(workingDir, testFile))
            .orElse(false);
    }

    private boolean hasRequiredTaikaiDependency(Path workingDir, Path testFile) {
        try {
            String source = Files.readString(testFile);
            return !source.contains("com.enofex.taikai") || List.of("pom.xml", "build.gradle", "build.gradle.kts")
                .stream()
                .map(workingDir::resolve)
                .anyMatch(buildFile -> buildFileContains(buildFile, "taikai"));
        }
        catch (IOException e) {
            LOGGER.error("Could not read {}: {}", testFile, e.getMessage(), e);
            return false;
        }
    }

    private Optional<Path> findTestClass(Path workingDir) {
        Path testRoot = workingDir.resolve("src/test/java");
        if (!Files.isDirectory(testRoot)) {
            return Optional.empty();
        }
        try (var stream = Files.walk(testRoot)) {
            return stream.filter(path -> path.getFileName().toString().equals(testClass + ".java")).findFirst();
        }
        catch (IOException e) {
            LOGGER.error("Could not search for {}: {}", testClass, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private static boolean buildFileContains(Path buildFile, String text) {
        if (!Files.isRegularFile(buildFile)) {
            return false;
        }
        try {
            return Files.readString(buildFile).contains(text);
        }
        catch (IOException e) {
            LOGGER.error("Could not read {}: {}", buildFile, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Runs the configured architecture test class via Maven and returns violations
     * derived from test failures. The {@code files} parameter is ignored.
     */
    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        int exitCode = runMaven(workingDir);
        String testFile = findTestClass(workingDir).map(workingDir::relativize)
            .map(Path::toString)
            .orElse(testClass + ".java");
        List<Violation> violations = parseSurefireReports(workingDir, testFile);
        if (exitCode != 0 && violations.isEmpty()) {
            LOGGER.atWarn()
                .addArgument(testClass)
                .log("Taikai: Maven exited with errors running '{}'. Check that taikai is on the test classpath.");
        }
        return violations;
    }

    int runMaven(Path workingDir) {
        List<String> command = buildMavenCommand(workingDir);
        LOGGER.debug("Running Taikai: {}", command);
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDir.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // Drain stdout/stderr to avoid blocking.
            try (InputStream out = process.getInputStream()) {
                out.transferTo(OutputStream.nullOutputStream());
            }
            int exitCode = process.waitFor();
            LOGGER.debug("Taikai Maven exit code: {}", exitCode);
            return exitCode;
        }
        catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to run Taikai Maven command: {}", e.getMessage(), e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return -1;
        }
    }

    private List<String> buildMavenCommand(Path workingDir) {
        String mvn;
        if (Files.isRegularFile(workingDir.resolve("mvnw"))) {
            mvn = "./mvnw";
        }
        else if (Files.isRegularFile(workingDir.resolve("mvnw.cmd"))) {
            mvn = "mvnw.cmd";
        }
        else {
            mvn = "mvn";
        }
        return List.of(mvn, "-q", "test", "-Dtest=" + testClass, "-Dsurefire.failIfNoSpecifiedTests=false");
    }

    private List<Violation> parseSurefireReports(Path workingDir, String testFile) {
        Path surefireDir = workingDir.resolve(SUREFIRE_DIR);
        if (!Files.isDirectory(surefireDir)) {
            LOGGER.debug("No Surefire reports directory found at {}", surefireDir);
            return List.of();
        }
        List<Path> reports = findReports(surefireDir);
        if (reports.isEmpty()) {
            return List.of();
        }
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
        // Disable external entity processing (XXE prevention).
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private List<Violation> extractViolations(Document doc, String testFile) {
        List<Violation> violations = new ArrayList<>();
        NodeList testcases = doc.getElementsByTagName("testcase");
        for (int i = 0; i < testcases.getLength(); i++) {
            Element tc = (Element) testcases.item(i);
            String methodName = tc.getAttribute("name");
            NodeList failures = tc.getElementsByTagName("failure");
            NodeList errors = tc.getElementsByTagName("error");
            if (failures.getLength() > 0) {
                violations.add(toViolation(methodName, (Element) failures.item(0), testFile));
            }
            else if (errors.getLength() > 0) {
                violations.add(toViolation(methodName, (Element) errors.item(0), testFile));
            }
        }
        return violations;
    }

    private static Violation toViolation(String methodName, Element element, String testFile) {
        String ruleId = TOOL_PREFIX + ":" + methodName;
        String rawMessage = element.getAttribute("message");
        String message = rawMessage.isBlank() ? element.getTextContent().strip() : rawMessage.strip();
        // Keep only the first line — Taikai messages can be very long.
        String firstLine = message.lines().findFirst().orElse(message);
        return new Violation(ruleId, testFile, 1, firstLine);
    }

    private static boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").redirectErrorStream(true).start();
            process.getInputStream().transferTo(OutputStream.nullOutputStream());
            return process.waitFor() == 0;
        }
        catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

}
