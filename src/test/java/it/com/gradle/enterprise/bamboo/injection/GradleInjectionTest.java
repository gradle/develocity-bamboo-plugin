package it.com.gradle.enterprise.bamboo.injection;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.google.common.collect.Iterables;
import com.gradle.develocity.bamboo.RemoteAgentProcess;
import com.gradle.develocity.bamboo.model.JobKey;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GradleInjectionTest extends AbstractInjectionTest {

    private static final String AGENT_VERSION = "3.14.1";

    private static RemoteAgentProcess bambooAgent;

    @RegisterExtension
    public final MockGeServer mockGeServer = new MockGeServer();

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
    void buildScanIsPublished(String buildKey) {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockGeServer.getAddress())
            .setDevelocityPluginVersion(AGENT_VERSION)
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, buildKey);
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        MockGeServer.ScanTokenRequest scanTokenRequest = mockGeServer.getLastScanTokenRequest();
        assertThat(scanTokenRequest, notNullValue());
        assertThat(scanTokenRequest.agentVersion, is(equalTo(AGENT_VERSION)));

        // and
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(mockGeServer.publicBuildScanId()));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("BUILD SUCCESSFUL"));

        assertThat(output, containsString("Publishing build scan..."));
        assertThat(output, containsString(mockGeServer.publicBuildScanId()));
    }

    @GradleProjectTest
    void buildScanNotPublishedWithoutAgentVersion(String buildKey) {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(mockGeServer.getAddress())
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
        assertThat(output, not(containsString(mockGeServer.publicBuildScanId())));
    }

    @GradleProjectTest
    void logsErrorIfBuildScanUploadFailed(String buildKey) {
        // given
        mockGeServer.rejectUpload();

        ensurePluginConfiguration(form -> form
            .setServer(mockGeServer.getAddress())
            .setDevelocityPluginVersion(AGENT_VERSION)
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
        assertThat(output, not(containsString(mockGeServer.publicBuildScanId())));
        assertThat(output, containsString("Publishing failed."));

        assertThat(output, containsString("Plugin version: " + AGENT_VERSION));
        assertThat(output, containsString("Request URL: " + String.format("%sscans/publish/gradle/%s/upload", mockGeServer.getAddress(), AGENT_VERSION)));
        assertThat(output, containsString("Response status code: 502"));
    }
}
