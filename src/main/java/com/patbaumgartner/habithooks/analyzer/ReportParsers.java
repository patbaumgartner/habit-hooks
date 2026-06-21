package com.patbaumgartner.habithooks.analyzer;

/** Factory methods for Maven report parsers. */
public final class ReportParsers {

    private ReportParsers() {
    }

    /** Creates a parser for SpotBugs XML reports. */
    public static MavenGoalAnalyzer.ReportParser spotbugsXml() {
        return (reportPath, workingDir, toolPrefix) -> SpotbugsReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for JaCoCo XML reports. */
    public static MavenGoalAnalyzer.ReportParser jacocoXml() {
        return (reportPath, workingDir, toolPrefix) -> JacocoReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for CycloneDX JSON SBOM files. */
    public static MavenGoalAnalyzer.ReportParser cyclonedxJson() {
        return (reportPath, workingDir, toolPrefix) -> CyclonedxReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for PIT mutation XML reports. */
    public static MavenGoalAnalyzer.ReportParser pitestXml() {
        return (reportPath, workingDir, toolPrefix) -> PitestReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for Maven PMD XML reports. */
    public static MavenGoalAnalyzer.ReportParser pmdXml() {
        return (reportPath, workingDir, toolPrefix) -> PmdXmlReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for CPD duplication XML reports. */
    public static MavenGoalAnalyzer.ReportParser cpdXml() {
        return (reportPath, workingDir, toolPrefix) -> CpdXmlReportParser.parse(reportPath, workingDir, toolPrefix);
    }

    /** Creates a parser for Spring Java Format validation output. */
    public static MavenGoalAnalyzer.ReportParser springJavaFormatText() {
        return (reportPath, workingDir, toolPrefix) -> TextReportParser.parseSpringJavaFormat(reportPath, workingDir,
                toolPrefix);
    }

    /** Creates a parser for Error Prone compiler output. */
    public static MavenGoalAnalyzer.ReportParser errorProneText() {
        return (reportPath, workingDir, toolPrefix) -> TextReportParser.parseErrorProne(reportPath, workingDir,
                toolPrefix);
    }

    /** Creates a parser for OWASP Dependency Check JSON reports. */
    public static MavenGoalAnalyzer.ReportParser owaspDependencyCheckJson() {
        return (reportPath, workingDir, toolPrefix) -> OwaspDependencyCheckReportParser.parse(reportPath, toolPrefix);
    }

}
