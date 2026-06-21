package com.patbaumgartner.habithooks.update;

import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InstallTargetTest {

    @Test
    void nativeAssetNameUsesPlatformTokens() {
        InstallTarget target = new InstallTarget("linux", "x64", true, Optional.empty());
        assertThat(target.assetName()).isEqualTo("habit-hooks-linux-x64");
    }

    @Test
    void jvmAssetNameUsesLauncherJar() {
        InstallTarget target = new InstallTarget("darwin", "arm64", false, Optional.empty());
        assertThat(target.assetName()).isEqualTo("habit-hooks-launcher.jar");
    }

    @Test
    void supportedRequiresKnownOsAndArch() {
        assertThat(new InstallTarget("linux", "x64", true, Optional.empty()).supported()).isTrue();
        assertThat(new InstallTarget("unsupported", "x64", true, Optional.empty()).supported()).isFalse();
        assertThat(new InstallTarget("linux", "unsupported", true, Optional.empty()).supported()).isFalse();
    }

    @Test
    void detectResolvesCurrentPlatform() {
        InstallTarget target = InstallTarget.detect();
        assertThat(target.os()).isNotBlank();
        assertThat(target.arch()).isNotBlank();
    }

}
