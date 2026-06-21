package com.patbaumgartner.habithooks.tasks;

import com.patbaumgartner.habithooks.model.AnalysisResult;
import com.patbaumgartner.habithooks.model.Violation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentTaskExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void groupsFindingsByRule() {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "A.java", 1, "big"),
                new Violation("pmd:GodClass", "B.java", 2, "big"),
                new Violation("owasp:CveHigh", "pom.xml", -1, "cve")), 2);

        List<AgentTask> tasks = new AgentTaskExporter().tasks(result);

        assertThat(tasks).hasSize(2);
        assertThat(tasks.get(0).ruleId()).isEqualTo("owasp:CveHigh");
        assertThat(tasks.get(1).count()).isEqualTo(2);
        assertThat(tasks.get(0).verificationCommand()).isEqualTo("habit-hooks --all");
        assertThat(tasks.get(0).acceptanceCriteria()).hasSize(3);
    }

    @Test
    void writesMarkdownAndJsonTaskExports() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "A.java", 1, "big")), 1);

        Path markdown = new AgentTaskExporter().write(result, tempDir, "markdown");
        Path json = new AgentTaskExporter().write(result, tempDir, "json");

        assertThat(Files.readString(markdown)).contains("habit-hooks agent tasks", "pmd:GodClass");
        assertThat(Files.readString(json)).contains("pmd:GodClass");
        assertThat(markdown).hasFileName("tasks.md");
        assertThat(json).hasFileName("tasks.json");
    }

    @Test
    void writesToExactTaskFileWhenOutputHasFormatExtension() throws Exception {
        AnalysisResult result = new AnalysisResult(List.of(new Violation("pmd:GodClass", "A.java", 1, "big")), 1);
        Path requestedOutput = tempDir.resolve("agent/petclinic-tasks.md");

        Path output = new AgentTaskExporter().write(result, requestedOutput, "markdown");

        assertThat(output).isEqualTo(requestedOutput);
        assertThat(Files.exists(requestedOutput)).isTrue();
        assertThat(Files.isDirectory(requestedOutput)).isFalse();
    }

    @Test
    void rejectsUnknownTaskFormat() {
        assertThatThrownBy(() -> AgentTaskExporter.Format.parse("xml")).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unsupported task format");
    }

}
