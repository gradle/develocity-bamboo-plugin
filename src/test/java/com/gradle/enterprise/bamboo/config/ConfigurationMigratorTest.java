package com.gradle.enterprise.bamboo.config;

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
import static org.mockito.Mockito.*;

class ConfigurationMigratorTest {

    private final BandanaManager bandanaManager = mock(BandanaManager.class);
    private final PluginEnabledEvent pluginEnabledEvent = mock(PluginEnabledEvent.class);

    @Test
    void runsMigrateConfigV0toV1() {
        setupPluginEnabledEventMock("com.gradle.enterprise.gradle-enterprise-bamboo-plugin");
        when(bandanaManager.getValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.ge.config")))
            .thenReturn(new PersistentConfiguration().setServer("https://mycomp"));

        new ConfigurationMigrator(bandanaManager, new JsonConfigurationConverter()).onPluginEnabled(pluginEnabledEvent);

        verify(bandanaManager, times(1)).setValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.ge.config.v1"),
            eq("{\"server\":\"https://mycomp\",\"allowUntrustedServer\":false,\"sharedCredentialName\":null,\"gePluginVersion\":null,\"ccudPluginVersion\":null,\"pluginRepository\":null,\"injectMavenExtension\":false,\"injectCcudExtension\":false}"));
        verify(bandanaManager, times(1)).removeValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.ge.config"));
    }

    @ParameterizedTest
    @MethodSource("configAndEventProvider")
    void doesNotRunMigrateConfigV0toV1(Object config, String eventKey) {
        setupPluginEnabledEventMock(eventKey);
        when(bandanaManager.getValue(any(BandanaContext.class), eq("com.gradle.bamboo.plugins.ge.config")))
            .thenReturn(config);

        new ConfigurationMigrator(bandanaManager, new JsonConfigurationConverter()).onPluginEnabled(pluginEnabledEvent);

        verify(bandanaManager, times(0)).setValue(any(BandanaContext.class), anyString(), anyString());
        verify(bandanaManager, times(0)).removeValue(any(BandanaContext.class), anyString());
    }

    static Stream<Arguments> configAndEventProvider() {
        return Stream.of(
            arguments(null, "com.gradle.enterprise.gradle-enterprise-bamboo-plugin"),
            arguments("not a Persistent Configuration object", "com.gradle.enterprise.gradle-enterprise-bamboo-plugin"),
            arguments(new PersistentConfiguration(), "some.other.plugin")
        );
    }

    private void setupPluginEnabledEventMock(String eventKey) {
        Plugin pluginMock = mock(Plugin.class);
        when(pluginMock.getKey()).thenReturn(eventKey);
        when(pluginEnabledEvent.getPlugin()).thenReturn(pluginMock);
    }

}
