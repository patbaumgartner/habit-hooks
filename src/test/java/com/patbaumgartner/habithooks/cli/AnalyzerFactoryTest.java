package com.patbaumgartner.habithooks.cli;

import com.patbaumgartner.habithooks.analyzer.Analyzer;
import com.patbaumgartner.habithooks.analyzer.CheckstyleAnalyzer;
import com.patbaumgartner.habithooks.analyzer.PmdAnalyzer;
import com.patbaumgartner.habithooks.config.HabitHooksConfig;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyzerFactoryTest {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    @AfterEach
    void clearNativeImageProperty() {
        System.clearProperty(NATIVE_IMAGE_PROPERTY);
    }

    @Test
    void createsCheckstyleAndPmdOnJvm() {
        List<Analyzer> analyzers = AnalyzerFactory.create(new HabitHooksConfig());

        assertThat(analyzers).anyMatch(CheckstyleAnalyzer.class::isInstance).anyMatch(PmdAnalyzer.class::isInstance);
    }

    @Test
    void skipsInProcessPmdInNativeImage() {
        System.setProperty(NATIVE_IMAGE_PROPERTY, "runtime");

        List<Analyzer> analyzers = AnalyzerFactory.create(new HabitHooksConfig());

        assertThat(analyzers).anyMatch(CheckstyleAnalyzer.class::isInstance).noneMatch(PmdAnalyzer.class::isInstance);
    }

}