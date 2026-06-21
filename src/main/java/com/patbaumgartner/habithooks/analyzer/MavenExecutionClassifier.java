package com.patbaumgartner.habithooks.analyzer;

import java.util.List;
import java.util.Map;

final class MavenExecutionClassifier {

    private static final Map<String, List<String>> TARGET_MARKERS = Map.ofEntries(
            Map.entry("spotbugs",
                    List.of("--- spotbugs:", "spotbugs-maven-plugin", "No plugin found for prefix 'spotbugs'")),
            Map.entry("jacoco", List.of(":report (", ":report-integration (")),
            Map.entry("cyclonedx",
                    List.of("--- cyclonedx:", "cyclonedx-maven-plugin", "No plugin found for prefix 'cyclonedx'")),
            Map.entry("pitest", List.of("--- pitest-maven:", "--- pitest:", "pitest-maven")),
            Map.entry("pmd", List.of("--- pmd:", "maven-pmd-plugin", "PMD version")),
            Map.entry("spring-javaformat",
                    List.of("--- spring-javaformat:", "spring-javaformat-maven-plugin",
                            "No plugin found for prefix 'spring-javaformat'")),
            Map.entry("errorprone", List.of("--- compiler:", "maven-compiler-plugin", "COMPILATION ERROR")),
            Map.entry("owasp", List.of("--- dependency-check:", "dependency-check-maven")));

    private MavenExecutionClassifier() {
    }

    static boolean lifecycleBlocked(String toolPrefix, int exitCode, String output) {
        return exitCode != 0 && mavenLifecycleFailed(output) && !targetStarted(toolPrefix, output);
    }

    private static boolean mavenLifecycleFailed(String output) {
        return output.contains("BUILD FAILURE") || output.contains("Failed to execute goal")
                || output.contains("[ERROR]");
    }

    private static boolean targetStarted(String toolPrefix, String output) {
        return TARGET_MARKERS.getOrDefault(toolPrefix, List.of(toolPrefix)).stream().anyMatch(output::contains);
    }

}
