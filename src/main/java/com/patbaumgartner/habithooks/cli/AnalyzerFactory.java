package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.analyzer.Analyzer;
import com.patbaumgartner.habithooks.analyzer.CapturingMavenGoalAnalyzer;
import com.patbaumgartner.habithooks.analyzer.CheckstyleAnalyzer;
import com.patbaumgartner.habithooks.analyzer.JSpecifyAnalyzer;
import com.patbaumgartner.habithooks.analyzer.MavenGoalAnalyzer;
import com.patbaumgartner.habithooks.analyzer.PmdAnalyzer;
import com.patbaumgartner.habithooks.analyzer.ReportParsers;
import com.patbaumgartner.habithooks.analyzer.TaikaiAnalyzer;
import com.patbaumgartner.habithooks.config.AnalyzerConfig;
import com.patbaumgartner.habithooks.config.HabitHooksConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Creates analyzer instances from habit-hooks configuration. */
final class AnalyzerFactory {

    private static final Map<String, String> DEFAULT_GOALS = Map.of("spotbugs", "spotbugs:spotbugs", "jacoco",
            "test jacoco:report", "cyclonedx", "cyclonedx:makeAggregateBom", "pitest",
            "-Pmutation-test test-compile org.pitest:pitest-maven:mutationCoverage", "spring-javaformat",
            "spring-javaformat:validate", "errorprone", "compile", "owasp",
            "org.owasp:dependency-check-maven:check -Dformat=JSON");

    private static final Map<String, String> DEFAULT_REPORTS = Map.of("spotbugs", "target/spotbugsXml.xml", "jacoco",
            "target/site/jacoco/jacoco.xml", "cyclonedx", "target/bom.json", "pitest",
            "target/pit-reports/mutations.xml", "spring-javaformat", "target/habit-hooks/spring-javaformat.log",
            "errorprone", "target/habit-hooks/errorprone.log", "owasp", "target/dependency-check-report.json");

    private static final Map<String, MavenGoalAnalyzer.ReportParser> PARSERS = Map.of("spotbugs",
            ReportParsers.spotbugsXml(), "jacoco", ReportParsers.jacocoXml(), "cyclonedx",
            ReportParsers.cyclonedxJson(), "pitest", ReportParsers.pitestXml(), "spring-javaformat",
            ReportParsers.springJavaFormatText(), "errorprone", ReportParsers.errorProneText(), "owasp",
            ReportParsers.owaspDependencyCheckJson());

    private static final Set<String> CAPTURING_ANALYZERS = Set.of("spring-javaformat", "errorprone");

    private static final List<String> MAVEN_ANALYZERS = List.of("spotbugs", "jacoco", "cyclonedx", "pitest",
            "spring-javaformat", "errorprone", "owasp");

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    private AnalyzerFactory() {
    }

    static List<Analyzer> create(HabitHooksConfig config) {
        List<Analyzer> analyzers = new ArrayList<>();
        Map<String, AnalyzerConfig> analyzerConfig = config.getAnalyzers();
        addCheckstyle(analyzers, analyzerConfig);
        addPmd(analyzers, analyzerConfig);
        addTaikai(analyzers, analyzerConfig);
        MAVEN_ANALYZERS.forEach(key -> addMaven(analyzers, analyzerConfig, key));
        addJSpecify(analyzers, analyzerConfig);
        return List.copyOf(analyzers);
    }

    private static void addCheckstyle(List<Analyzer> analyzers, Map<String, AnalyzerConfig> analyzerConfig) {
        AnalyzerConfig checkstyle = analyzerConfig.getOrDefault("checkstyle", new AnalyzerConfig());
        if (checkstyle.isEnabled()) {
            analyzers.add(new CheckstyleAnalyzer(checkstyle.getConfigFile()));
        }
    }

    private static void addPmd(List<Analyzer> analyzers, Map<String, AnalyzerConfig> analyzerConfig) {
        AnalyzerConfig pmd = analyzerConfig.getOrDefault("pmd", new AnalyzerConfig());
        if (pmd.isEnabled() && !isNativeImage()) {
            analyzers.add(new PmdAnalyzer(pmd.getRulesets()));
        }
    }

    private static boolean isNativeImage() {
        return System.getProperty(NATIVE_IMAGE_PROPERTY) != null;
    }

    private static void addTaikai(List<Analyzer> analyzers, Map<String, AnalyzerConfig> analyzerConfig) {
        AnalyzerConfig taikai = analyzerConfig.get("taikai");
        if (taikai != null && taikai.isEnabled()) {
            analyzers.add(new TaikaiAnalyzer(taikai.getTestClass()));
        }
    }

    private static void addMaven(List<Analyzer> analyzers, Map<String, AnalyzerConfig> analyzerConfig, String key) {
        AnalyzerConfig analyzer = analyzerConfig.get(key);
        if (analyzer != null && analyzer.isEnabled()) {
            analyzers.add(createMaven(key, analyzer));
        }
    }

    private static void addJSpecify(List<Analyzer> analyzers, Map<String, AnalyzerConfig> analyzerConfig) {
        AnalyzerConfig analyzer = analyzerConfig.get("jspecify");
        if (analyzer != null && analyzer.isEnabled()) {
            analyzers.add(new JSpecifyAnalyzer());
        }
    }

    private static MavenGoalAnalyzer createMaven(String key, AnalyzerConfig analyzer) {
        String goal = configuredValue(analyzer.getGoal(), DEFAULT_GOALS.get(key));
        String report = configuredValue(analyzer.getReportFile(), DEFAULT_REPORTS.get(key));
        if (CAPTURING_ANALYZERS.contains(key)) {
            return new CapturingMavenGoalAnalyzer(key, goal, report, PARSERS.get(key));
        }
        return new MavenGoalAnalyzer(key, goal, report, PARSERS.get(key));
    }

    private static String configuredValue(String configured, String defaultValue) {
        return configured == null ? defaultValue : configured;
    }

}
