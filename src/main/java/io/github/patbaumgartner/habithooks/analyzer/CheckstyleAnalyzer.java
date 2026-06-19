package io.github.patbaumgartner.habithooks.analyzer;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import io.github.patbaumgartner.habithooks.model.Violation;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the Checkstyle API to report violations as {@link Violation} records.
 *
 * <p>Uses {@code checkstyle.xml} in the working directory as the rule configuration.
 * Violations carry rule IDs prefixed with {@code "checkstyle:"}.
 */
public final class CheckstyleAnalyzer implements Analyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckstyleAnalyzer.class);
    private static final String TOOL_PREFIX = "checkstyle";
    private static final String DEFAULT_CONFIG = "checkstyle.xml";

    private final String configFile;

    /**
     * Creates an analyzer using the given Checkstyle config file name
     * (relative to the working directory).
     *
     * @param configFile config file name, e.g. {@code "checkstyle.xml"}
     */
    public CheckstyleAnalyzer(String configFile) {
        this.configFile = configFile;
    }

    /** Creates an analyzer using the default {@code checkstyle.xml} config name. */
    public CheckstyleAnalyzer() {
        this(DEFAULT_CONFIG);
    }

    @Override
    public String toolPrefix() {
        return TOOL_PREFIX;
    }

    @Override
    public boolean isAvailable(Path workingDir) {
        return Files.isRegularFile(workingDir.resolve(configFile));
    }

    @Override
    public List<Violation> analyze(List<Path> files, Path workingDir) {
        if (files.isEmpty()) {
            return List.of();
        }
        Path configPath = workingDir.resolve(configFile);
        try {
            return runCheckstyle(files, configPath);
        } catch (CheckstyleException e) {
            LOGGER.warn("Checkstyle analysis failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Violation> runCheckstyle(List<Path> files, Path configPath)
            throws CheckstyleException {
        Configuration config = loadConfig(configPath);
        CollectingAuditListener listener = new CollectingAuditListener();
        Checker checker = buildChecker(config, listener);

        List<File> javaFiles = files.stream()
                .map(Path::toFile)
                .toList();
        checker.process(javaFiles);
        checker.destroy();

        return listener.getViolations();
    }

    private static Configuration loadConfig(Path configPath) throws CheckstyleException {
        return ConfigurationLoader.loadConfiguration(
                configPath.toString(),
                new PropertiesExpander(new Properties()),
                ConfigurationLoader.IgnoredModulesOptions.OMIT);
    }

    private static Checker buildChecker(
            Configuration config,
            AuditListener listener) throws CheckstyleException {
        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(config);
        checker.addListener(listener);
        return checker;
    }

    /** Collects Checkstyle audit events into a {@link Violation} list. */
    private static final class CollectingAuditListener implements AuditListener {

        private final List<Violation> violations = new ArrayList<>();

        @Override
        public void auditStarted(AuditEvent event) {
            // no-op
        }

        @Override
        public void auditFinished(AuditEvent event) {
            // no-op
        }

        @Override
        public void fileStarted(AuditEvent event) {
            // no-op
        }

        @Override
        public void fileFinished(AuditEvent event) {
            // no-op
        }

        @Override
        public void addError(AuditEvent event) {
            String ruleId = TOOL_PREFIX + ":" + extractRuleName(event.getSourceName());
            violations.add(new Violation(
                    ruleId,
                    relativize(event.getFileName()),
                    event.getLine(),
                    event.getMessage()));
        }

        @Override
        public void addException(AuditEvent event, Throwable throwable) {
            LOGGER.warn("Checkstyle exception on {}: {}", event.getFileName(),
                    throwable.getMessage());
        }

        List<Violation> getViolations() {
            return List.copyOf(violations);
        }

        private static final int CHECK_SUFFIX_LENGTH = 5; // length of "Check"

        private static String extractRuleName(String sourceName) {
            if (sourceName == null) {
                return "Unknown";
            }
            int lastDot = sourceName.lastIndexOf('.');
            String simple = lastDot >= 0 ? sourceName.substring(lastDot + 1) : sourceName;
            // Checkstyle module class names end in "Check"; strip it to match module names
            if (simple.endsWith("Check")) {
                return simple.substring(0, simple.length() - CHECK_SUFFIX_LENGTH);
            }
            return simple;
        }

        private static String relativize(String absolutePath) {
            if (absolutePath == null) {
                return "";
            }
            Path abs = Path.of(absolutePath);
            Path cwd = Path.of(System.getProperty("user.dir", "."));
            try {
                return cwd.relativize(abs).toString();
            } catch (IllegalArgumentException e) {
                return absolutePath;
            }
        }
    }
}
