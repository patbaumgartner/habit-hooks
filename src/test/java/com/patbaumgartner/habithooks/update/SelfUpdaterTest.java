package com.patbaumgartner.habithooks.update;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class SelfUpdaterTest {

    private static final String LATEST_JSON = "{\"tag_name\": \"v0.2.0\", \"name\": \"habit-hooks 0.2.0\"}";

    @Test
    void parseTagExtractsTagName() {
        assertThat(SelfUpdater.parseTag(LATEST_JSON)).contains("v0.2.0");
    }

    @Test
    void parseTagReturnsEmptyWhenMissing() {
        assertThat(SelfUpdater.parseTag("{\"name\": \"x\"}")).isEmpty();
    }

    @Test
    void normalizeStripsLeadingV() {
        assertThat(SelfUpdater.normalize("v1.2.3")).isEqualTo("1.2.3");
        assertThat(SelfUpdater.normalize(" 1.2.3 ")).isEqualTo("1.2.3");
    }

    @Test
    void reportsAlreadyUpToDate() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InstallTarget target = new InstallTarget("linux", "x64", false, Optional.empty());
        SelfUpdater updater = new SelfUpdater("0.2.0", new PrintStream(buffer), new FakeFetcher(LATEST_JSON), target);

        int exit = updater.run();

        assertThat(exit).isZero();
        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("Already up to date");
    }

    @Test
    void replacesArtifactWhenNewerVersionAvailable(@TempDir Path dir) throws IOException {
        Path artifact = Files.writeString(dir.resolve("habit-hooks"), "OLD");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InstallTarget target = new InstallTarget("linux", "x64", true, Optional.of(artifact));
        SelfUpdater updater = new SelfUpdater("0.1.0", new PrintStream(buffer),
                new FakeFetcher(LATEST_JSON, "NEW-BINARY"), target);

        int exit = updater.run();

        assertThat(exit).isZero();
        assertThat(Files.readString(artifact)).isEqualTo("NEW-BINARY");
        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("Updated to 0.2.0");
    }

    @Test
    void failsWhenArtifactCannotBeLocated() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InstallTarget target = new InstallTarget("linux", "x64", false, Optional.empty());
        SelfUpdater updater = new SelfUpdater("0.1.0", new PrintStream(buffer), new FakeFetcher(LATEST_JSON), target);

        int exit = updater.run();

        assertThat(exit).isEqualTo(1);
        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("Cannot locate");
    }

    @Test
    void failsWhenLatestReleaseUnavailable() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        InstallTarget target = new InstallTarget("linux", "x64", false, Optional.empty());
        SelfUpdater updater = new SelfUpdater("0.1.0", new PrintStream(buffer), new FailingFetcher(), target);

        int exit = updater.run();

        assertThat(exit).isEqualTo(1);
        assertThat(buffer.toString(StandardCharsets.UTF_8)).contains("Could not determine");
    }

    private static final class FakeFetcher implements HttpFetcher {

        private final String json;

        private final String payload;

        FakeFetcher(String json) {
            this(json, "");
        }

        FakeFetcher(String json, String payload) {
            this.json = json;
            this.payload = payload;
        }

        @Override
        public String getText(String url) {
            return this.json;
        }

        @Override
        public void downloadTo(String url, Path destination) throws IOException {
            Files.writeString(destination, this.payload);
        }

    }

    private static final class FailingFetcher implements HttpFetcher {

        @Override
        public String getText(String url) throws IOException {
            throw new IOException("network down");
        }

        @Override
        public void downloadTo(String url, Path destination) throws IOException {
            throw new IOException("network down");
        }

    }

}
