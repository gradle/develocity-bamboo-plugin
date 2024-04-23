package it.com.gradle.develocity.bamboo.injection;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.google.common.collect.Iterables;
import com.gradle.develocity.bamboo.RemoteAgentProcess;
import com.gradle.develocity.bamboo.model.JobKey;
import it.com.gradle.develocity.bamboo.MockDevelocityServer;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GradleInjectionTest extends AbstractInjectionTest {

    private static final String GRADLE_ENTERPRISE_PLUGIN_VERSION = "3.16.2";
    private static final String DEVELOCITY_PLUGIN_VERSION = "3.17";

    private static RemoteAgentProcess bambooAgent;

    @RegisterExtension
    public final MockDevelocityServer mockDevelocityServer = new MockDevelocityServer();

    @BeforeAll
    static void startBambooAgent() {
        assumeTrue(SystemUtils.IS_OS_UNIX);

        bambooAgent = new RemoteAgentProcess(BAMBOO, bambooApi);
        bambooAgent.startAgentAndWaitForReadiness();
    }

    @AfterAll
    static void tearDownBambooAgent() {
        if (bambooAgent != null) {
            try {
                bambooAgent.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @GradleProjectTest
    void buildScanIsPublishedWithDevelocityPlugin(String buildKey) {
        assertBuildScanPublished(buildKey, DEVELOCITY_PLUGIN_VERSION);
    }

    @GradleProjectTest
    void buildScanIsPublishedWithGradleEnterprisePlugin(String buildKey) {
        assertBuildScanPublished(buildKey, GRADLE_ENTERPRISE_PLUGIN_VERSION);
    }

    void assertBuildScanPublished(String buildKey, String agentVersion) {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockDevelocityServer.getAddress())
            .setDevelocityPluginVersion(agentVersion)
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, buildKey);
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        MockDevelocityServer.ScanTokenRequest scanTokenRequest = mockDevelocityServer.getLastScanTokenRequest();
        assertThat(scanTokenRequest, notNullValue());
        assertThat(scanTokenRequest.agentVersion, is(equalTo(agentVersion)));

        // and
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(mockDevelocityServer.publicBuildScanId()));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("Setting captureFileFingerprints: true")); // default setting is true

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, containsString("Publishing build scan..."));
        assertThat(output, containsString(mockDevelocityServer.publicBuildScanId()));
        assertThat(output, not(containsString("The deprecated \"gradleEnterprise.")));
    }

    @GradleProjectTest
    void buildScanNotPublishedWithoutAgentVersion(String buildKey) {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockDevelocityServer.getAddress())
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, buildKey);
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, nullValue());

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, not(containsString("Publishing build scan...")));
        assertThat(output, not(containsString(mockDevelocityServer.publicBuildScanId())));
    }

    @GradleProjectTest
    void logsErrorIfBuildScanUploadFailed(String buildKey) {
        // given
        mockDevelocityServer.rejectUpload();

        ensurePluginConfiguration(form -> form
            .setServer(mockDevelocityServer.getAddress())
            .setDevelocityPluginVersion(DEVELOCITY_PLUGIN_VERSION)
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, buildKey);
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, containsString("Publishing build scan..."));
        assertThat(output, not(containsString(mockDevelocityServer.publicBuildScanId())));
        assertThat(output, containsString("Publishing failed."));

        assertThat(output, containsString("Plugin version: " + DEVELOCITY_PLUGIN_VERSION));
        assertThat(output, containsString("Request URL: " + String.format("%sscans/publish/gradle/%s/upload", mockDevelocityServer.getAddress(), DEVELOCITY_PLUGIN_VERSION)));
        assertThat(output, containsString("Response status code: 502"));
    }

    @GradleProjectTest
    void skipsAutoInjectionIfRepositoryShouldBeExcluded(String buildKey) {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockDevelocityServer.getAddress())
            .setDevelocityPluginVersion(DEVELOCITY_PLUGIN_VERSION)
            .setVcsRepositoryFilter("-:simple")
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, buildKey);
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        MockDevelocityServer.ScanTokenRequest scanTokenRequest = mockDevelocityServer.getLastScanTokenRequest();
        assertThat(scanTokenRequest, nullValue());

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, not(containsString("Publishing build scan...")));
    }

    @Test
    void enforceUrlOverridesConfiguredUrlInProject() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer("http://localhost:8888")
            .setDevelocityPluginVersion(DEVELOCITY_PLUGIN_VERSION)
            .enforceUrl()
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "GPPA");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(null));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, containsString("Publishing build scan failed due to network error"));
        assertThat(output, containsString("http://localhost:8888/"));
    }

    @Test
    void buildScanLinksAreDetectedForMultipleTasks() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockDevelocityServer.getAddress())
            .setDevelocityPluginVersion(DEVELOCITY_PLUGIN_VERSION)
        );

        // Project with 2 tasks
        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "GPMT");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        List<String> buildScans = getBuildScansFromMetadata(planResultKey).map(s -> Arrays.asList(s.split(","))).orElse(null);
        assertThat(buildScans, notNullValue());
        assertThat(buildScans.size(), equalTo(2));
        assertThat(buildScans, everyItem(containsString(mockDevelocityServer.getAddress().toString())));
    }

}
