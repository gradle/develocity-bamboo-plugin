package com.gradle.develocity.bamboo.config;

import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurationMigratorTest {

    private final BandanaManager bandanaManager = mock(BandanaManager.class);
    private final PluginEnabledEvent pluginEnabledEvent = mock(PluginEnabledEvent.class);

    @Test
    void runsMigrateConfigV0ToV1() {
        setupPluginEnabledEventMock("com.gradle.develocity.develocity-bamboo-plugin");
        when(bandanaManager.getValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.develocity.config")))
            .thenReturn(new PersistentConfiguration().setServer("https://mycomp"));

        new ConfigurationMigrator(bandanaManager).onPluginEnabled(pluginEnabledEvent);

        verify(bandanaManager, times(1)).setValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.develocity.config.v1"),
            eq("{\"server\":\"https://mycomp\",\"allowUntrustedServer\":false,\"sharedCredentialName\":null,\"enforceUrl\":false,\"develocityPluginVersion\":null,\"ccudPluginVersion\":null,\"pluginRepository\":null,\"pluginRepositoryCredentialName\":null,\"injectMavenExtension\":false,\"injectCcudExtension\":false,\"mavenExtensionCustomCoordinates\":null,\"ccudExtensionCustomCoordinates\":null,\"vcsRepositoryFilter\":null,\"shortLivedTokenExpiry\":null,\"gradleCaptureFileFingerprints\":false,\"mavenCaptureFileFingerprints\":false}"));
        verify(bandanaManager, times(1)).removeValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.develocity.config"));
    }

    @ParameterizedTest
    @MethodSource("configAndEventProvider")
    void doesNotRunMigrateConfigV0ToV1(Object config, String eventKey) {
        setupPluginEnabledEventMock(eventKey);
        when(bandanaManager.getValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.develocity.config")))
            .thenReturn(config);

        new ConfigurationMigrator(bandanaManager).onPluginEnabled(pluginEnabledEvent);

        verify(bandanaManager, times(0)).setValue(any(BandanaContext.class), anyString(), anyString());
        verify(bandanaManager, times(0)).removeValue(any(BandanaContext.class), anyString());
    }

    static Stream<Arguments> configAndEventProvider() {
        return Stream.of(
            arguments(null, "com.gradle.develocity.develocity-bamboo-plugin"),
            arguments("not a Persistent Configuration object", "com.gradle.develocity.develocity-bamboo-plugin"),
            arguments(new PersistentConfiguration(), "some.other.plugin")
        );
    }

    private void setupPluginEnabledEventMock(String eventKey) {
        Plugin pluginMock = mock(Plugin.class);
        when(pluginMock.getKey()).thenReturn(eventKey);
        when(pluginEnabledEvent.getPlugin()).thenReturn(pluginMock);
    }

}
