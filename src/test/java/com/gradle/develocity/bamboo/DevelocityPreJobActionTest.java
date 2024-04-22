package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.credentials.CredentialsAccessor;
import com.atlassian.bamboo.credentials.CredentialsData;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.bandana.BandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.config.UsernameAndPassword;
import com.gradle.develocity.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DevelocityPreJobActionTest {

    private final BandanaManager bandanaManager = mock(BandanaManager.class);
    private final CredentialsAccessor credentialsAccessor = mock(CredentialsAccessor.class);

    private final StageExecution stageExecution = mock(StageExecution.class);
    private final BuildContext buildContext = mock(BuildContext.class);
    private final VariableContext variableContext = mock(VariableContext.class);

    private final ShortLivedTokenClient mockShortLivedTokenClient = mock(ShortLivedTokenClient.class);

    private final GradleBuildScanInjector gradleBuildScanInjector =
            new GradleBuildScanInjector(null, null, null, null, null);

    private final DevelocityPreJobAction develocityPreJobAction =
            new DevelocityPreJobAction(
                    new PersistentConfigurationManager(bandanaManager),
                    new UsernameAndPasswordCredentialsProvider(credentialsAccessor),
                    Collections.singletonList(gradleBuildScanInjector),
                    mockShortLivedTokenClient
            );

    @Test
    void doesNothingIfNoConfiguration() {
        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(bandanaManager, times(1)).getValue(any(BandanaContext.class), anyString());
        verifyNoInteractions(credentialsAccessor);
    }

    @Test
    void doesNothingIfNoSharedCredentials() {
        // given
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{}");

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(bandanaManager, times(1)).getValue(any(BandanaContext.class), anyString());
        verifyNoInteractions(credentialsAccessor);
    }

    @Test
    void doesNothingIfCredentialsNotFound() {
        // given
        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"sharedCredentialName\":\"" + credentialsName + "\"}");

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(credentialsAccessor, times(1)).getCredentialsByName(credentialsName);
        verifyNoInteractions(buildContext);
    }

    @Test
    void doesNothingIfCredentialsWithoutPassword() {
        // given
        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.emptyMap());

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"sharedCredentialName\":\"" + credentialsName + "\"}");
        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(credentialsAccessor, times(1)).getCredentialsByName(credentialsName);
        verifyNoInteractions(buildContext);
    }

    @Test
    void doesNothingIfNoSupportedTasks() {
        // given
        String accessKey = String.format("scans.gradle.com=%s", RandomStringUtils.randomAlphanumeric(10));
        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\", " +
                        "\"develocityPluginVersion\": \"3.12\"}");

        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn("unsupported_plugin_key");
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        assertThat(gradleBuildScanInjector.hasSupportedTasks(buildContext), is(false));
        verify(buildContext, never()).getVariableContext();
    }

    @Test
    void doesNothingIfInjectionDisabled() {
        // given
        String accessKey = String.format("scans.gradle.com=%s", RandomStringUtils.randomAlphanumeric(10));
        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\"}");
        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn(GradleBuildScanInjector.SCRIPT_PLUGIN_KEY);
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        assertThat(gradleBuildScanInjector.hasSupportedTasks(buildContext), is(true));
        verify(buildContext, never()).getVariableContext();
    }

    @Test
    void doesNothingIfNoShortLivedTokenRetrieved() {
        // given
        String accessKey = String.format("scans.gradle.com=%s", RandomStringUtils.randomAlphanumeric(10));
        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));
        when(mockShortLivedTokenClient.get(anyString(), any(), anyString())).thenReturn(Optional.empty());

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\"}");
        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn(GradleBuildScanInjector.SCRIPT_PLUGIN_KEY);
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        assertThat(gradleBuildScanInjector.hasSupportedTasks(buildContext), is(true));
        verify(buildContext, never()).getVariableContext();
        verify(variableContext, never()).addLocalVariable(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            GradleBuildScanInjector.BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_KEY,
            GradleBuildScanInjector.BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_WRAPPER_KEY,
            GradleBuildScanInjector.BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLEW_KEY,
            GradleBuildScanInjector.SCRIPT_PLUGIN_KEY,
            GradleBuildScanInjector.COMMAND_PLUGIN_KEY,
            "org.jfrog.bamboo." + GradleBuildScanInjector.ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX
    })
    void addsAccessKeyToContext(String pluginKey) {
        // given
        String accessKey = String.format("scans.gradle.com=%s", RandomStringUtils.randomAlphanumeric(10));
        String shortLivedToken = RandomStringUtils.randomAlphanumeric(10);

        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));
        when(mockShortLivedTokenClient.get(anyString(), any(), any())).thenReturn(Optional.of(DevelocityAccessCredentials.HostnameAccessKey.of("scans.gradle.com", shortLivedToken)));

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\", " +
                        "\"develocityPluginVersion\": \"3.12\"}");

        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn(pluginKey);
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));
        when(buildContext.getVariableContext()).thenReturn(variableContext);

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(variableContext, times(1)).addLocalVariable(Constants.ACCESS_KEY, "scans.gradle.com=" + shortLivedToken);
    }

    @Test
    void addsAccessKeyToContextWithEnforceURL() {
        // given
        String accessKey = String.format("scans.gradle.com=%s;localhost=%s", RandomStringUtils.randomAlphanumeric(10),RandomStringUtils.randomAlphanumeric(10));
        String shortLivedToken = RandomStringUtils.randomAlphanumeric(10);

        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));
        when(mockShortLivedTokenClient.get(anyString(), any(), any())).thenReturn(Optional.of(DevelocityAccessCredentials.HostnameAccessKey.of("scans.gradle.com", shortLivedToken)));

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\", " +
                        "\"develocityPluginVersion\": \"3.17\",\"enforceUrl\": true}");

        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn(GradleBuildScanInjector.SCRIPT_PLUGIN_KEY);
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));
        when(buildContext.getVariableContext()).thenReturn(variableContext);

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(variableContext, times(1)).addLocalVariable(Constants.ACCESS_KEY, "scans.gradle.com=" + shortLivedToken);
    }

    @Test
    void addsAccessKeyWithMultipleValuesForEachHost() {
        // given
        String accessKey = String.format("scans.gradle.com=%s;localhost=%s", RandomStringUtils.randomAlphanumeric(10),RandomStringUtils.randomAlphanumeric(10));
        String shortLivedTokenA = RandomStringUtils.randomAlphanumeric(10);
        String shortLivedTokenB = RandomStringUtils.randomAlphanumeric(10);

        CredentialsData credentialsData = mock(CredentialsData.class);
        when(credentialsData.getPluginKey()).thenReturn(UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY);
        when(credentialsData.getConfiguration()).thenReturn(Collections.singletonMap(UsernameAndPassword.PASSWORD, accessKey));
        when(mockShortLivedTokenClient.get(anyString(), any(), any()))
                .thenReturn(Optional.of(DevelocityAccessCredentials.HostnameAccessKey.of("scans.gradle.com", shortLivedTokenA)))
                .thenReturn(Optional.of(DevelocityAccessCredentials.HostnameAccessKey.of("localhost", shortLivedTokenB)));

        String credentialsName = RandomStringUtils.randomAlphanumeric(10);
        when(bandanaManager.getValue(any(BandanaContext.class), anyString()))
                .thenReturn("{\"server\":\"https://scans.gradle.com\",\"sharedCredentialName\":\"" + credentialsName + "\", " +
                        "\"develocityPluginVersion\": \"3.17\"}");

        when(credentialsAccessor.getCredentialsByName(credentialsName))
                .thenReturn(credentialsData);

        RuntimeTaskDefinition runtimeTaskDefinition = mock(RuntimeTaskDefinition.class);
        when(runtimeTaskDefinition.isEnabled()).thenReturn(true);
        when(runtimeTaskDefinition.getPluginKey()).thenReturn(GradleBuildScanInjector.SCRIPT_PLUGIN_KEY);
        when(buildContext.getRuntimeTaskDefinitions()).thenReturn(Collections.singletonList(runtimeTaskDefinition));
        when(buildContext.getVariableContext()).thenReturn(variableContext);

        // when
        develocityPreJobAction.execute(stageExecution, buildContext);

        // then
        verify(variableContext, times(1)).addLocalVariable(Constants.ACCESS_KEY, String.format("scans.gradle.com=%s;localhost=%s", shortLivedTokenA, shortLivedTokenB));
    }
}
