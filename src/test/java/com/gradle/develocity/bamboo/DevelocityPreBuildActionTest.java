package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.ResultKey;
import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevelocityPreBuildActionTest {

    private static final RuntimeException INJECTION_EXCEPTION = new RuntimeException();

    @Test
    void preBuildActionDoesNotFailOnException() {
        BuildScanInjector<?> mockBuildScanInjector = mock(BuildScanInjector.class);
        BuildLoggerManager mockBuildLoggerManager = mock(BuildLoggerManager.class);

        doThrow(INJECTION_EXCEPTION).when(mockBuildScanInjector).inject(any());
        when(mockBuildScanInjector.buildTool()).thenReturn(BuildTool.GRADLE);

        BuildLogger mockBuildLogger = mock(BuildLogger.class);
        when(mockBuildLoggerManager.getLogger(any(ResultKey.class))).thenReturn(mockBuildLogger);

        DevelocityPreBuildAction develocityPreBuildAction =
            new DevelocityPreBuildAction(
                Collections.singletonList(mockBuildScanInjector), mockBuildLoggerManager, mock(PersistentConfigurationManager.class));

        develocityPreBuildAction.init(TestFixtures.getBuildContext());

        assertDoesNotThrow(develocityPreBuildAction::call);
        verify(mockBuildLoggerManager, times(1)).getLogger(any(ResultKey.class));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockBuildLogger).addErrorLogEntry(argumentCaptor.capture(), eq(INJECTION_EXCEPTION));
        assertEquals("Develocity Gradle auto-injection failed", argumentCaptor.getValue());
    }

    @Test
    void injectionSucceedsForOneAndFailsForAnother() {
        BuildScanInjector<?> mockSuccessfulBuildScanInjector = mock(BuildScanInjector.class);
        BuildScanInjector<?> mockFailedBuildScanInjector = mock(BuildScanInjector.class);
        BuildLoggerManager mockBuildLoggerManager = mock(BuildLoggerManager.class);

        doThrow(INJECTION_EXCEPTION).when(mockFailedBuildScanInjector).inject(any());
        when(mockFailedBuildScanInjector.buildTool()).thenReturn(BuildTool.GRADLE);

        BuildLogger mockBuildLogger = mock(BuildLogger.class);
        when(mockBuildLoggerManager.getLogger(any(ResultKey.class))).thenReturn(mockBuildLogger);

        DevelocityPreBuildAction develocityPreBuildAction =
            new DevelocityPreBuildAction(
                Arrays.asList(mockSuccessfulBuildScanInjector, mockFailedBuildScanInjector), mockBuildLoggerManager, mock(PersistentConfigurationManager.class));

        develocityPreBuildAction.init(TestFixtures.getBuildContext());

        assertDoesNotThrow(develocityPreBuildAction::call);

        verify(mockSuccessfulBuildScanInjector, times(1)).inject(any(BuildContext.class));
        verify(mockFailedBuildScanInjector, times(1)).inject(any(BuildContext.class));

        verify(mockBuildLoggerManager, times(1)).getLogger(any(ResultKey.class));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockBuildLogger).addErrorLogEntry(argumentCaptor.capture(), eq(INJECTION_EXCEPTION));
        assertEquals("Develocity Gradle auto-injection failed", argumentCaptor.getValue());
    }

}
