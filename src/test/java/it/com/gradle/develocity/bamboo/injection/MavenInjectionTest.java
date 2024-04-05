package it.com.gradle.develocity.bamboo.injection;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.google.common.collect.Iterables;
import com.gradle.develocity.bamboo.RemoteAgentProcess;
import com.gradle.develocity.bamboo.model.JobKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

public class MavenInjectionTest extends AbstractInjectionTest {

    private static final String PUBLIC_DEVELOCITY_SERVER = "https://scans.gradle.com";

    private static final String JAVA_HOME_PROP = "java.home";
    private static final Collection<String> MAVEN_HOME_ENV_VARS = Arrays.asList("M2_HOME", "MAVEN_HOME");

    private static RemoteAgentProcess bambooAgent;

    @BeforeAll
    static void startBambooAgent() {
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

    @BeforeEach
    void beforeEach() {
        setupJavaHome(determineJavaHome());
        setupMaven3(determineMavenHome());
    }

    private static String determineJavaHome() {
        String javaHome = System.getProperty(JAVA_HOME_PROP);
        Validate.validState(
            StringUtils.isNotBlank(javaHome), "%s system property is not set", JAVA_HOME_PROP);

        return javaHome;
    }

    private static String determineMavenHome() {
        return MAVEN_HOME_ENV_VARS
            .stream()
            .map(System::getenv)
            .filter(StringUtils::isNotBlank)
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException(
                    String.format("None of the following environment variables is set: %s", MAVEN_HOME_ENV_VARS)));
    }

    @Test
    void buildScanIsPublished() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(PUBLIC_DEVELOCITY_SERVER)
            .enableDevelocityExtensionAutoInjection()
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPA");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);
        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, startsWith("https://gradle.com/s/"));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, containsString("[INFO] Publishing build scan..."));
        assertThat(output, containsString("[INFO] https://gradle.com/s/"));
    }

    @Test
    void buildScanNotPublishedWithoutExtension() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(PUBLIC_DEVELOCITY_SERVER)
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPA");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, nullValue());

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, not(containsString("[INFO] Publishing build scan...")));
        assertThat(output, not(containsString("[INFO] https://gradle.com/s/")));
    }

    @Test
    void buildScanNotPublishedWithoutAcceptingTermsOfUse() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(PUBLIC_DEVELOCITY_SERVER)
            .enableDevelocityExtensionAutoInjection()
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPM");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, nullValue());

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, containsString("[INFO] The build scan was not published due to a configuration problem."));
        assertThat(output, containsString("[INFO] The Gradle Terms of Use have not been agreed to."));
    }
    @Test
    void extensionAlreadyAppliedInProjectAndBuildScanAttemptedToPublishToProjectConfiguredHost() {
        // given
        ensurePluginConfiguration(form -> form
                .setServer(PUBLIC_DEVELOCITY_SERVER)
                .enableDevelocityExtensionAutoInjection()
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPEA");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(null));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, containsString("[WARNING] Unexpected error while contacting Gradle Enterprise server at http://localhost:8080/"));
    }

    @Test
    void extensionAlreadyAppliedAndEnforceUrl() {
        // given
        ensurePluginConfiguration(form -> form
                .setServer("http://localhost:8888")
                .enableDevelocityExtensionAutoInjection()
                .enforceUrl()
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPEA");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(null));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, containsString("[WARNING] Unexpected error while contacting Gradle Enterprise server at http://localhost:8888/"));
    }

    @Test
    void customExtensionAlreadyApplied() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(PUBLIC_DEVELOCITY_SERVER)
            .enableDevelocityExtensionAutoInjection()
            .setMavenExtensionCustomCoordinates("org.apache.maven.extensions:maven-enforcer-extension")
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPCE");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(null));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, not(containsString("[INFO] The Gradle Terms of Use have not been agreed to.")));
    }

    @Test
    void skipsAutoInjectionIfRepositoryShouldBeExcluded() {
        // given
        ensurePluginConfiguration(form -> form
            .setServer(PUBLIC_DEVELOCITY_SERVER)
            .enableDevelocityExtensionAutoInjection()
            .setVcsRepositoryFilter("-:simple")
        );

        PlanKey planKey = PlanKeys.getPlanKey(PROJECT_KEY, "MPCE");
        JobKey jobKey = Iterables.getOnlyElement(bambooApi.getJobs(planKey)).getKey();

        // when
        PlanResultKey planResultKey = triggerBuild(planKey, jobKey);
        waitForBuildToFinish(planResultKey);

        // then
        String buildScans = getBuildScansFromMetadata(planResultKey).orElse(null);
        assertThat(buildScans, equalTo(null));

        // and
        String output = bambooApi.getLog(planResultKey);

        assertThat(output, containsString("[INFO] BUILD SUCCESS"));

        assertThat(output, not(containsString("[INFO] The Gradle Terms of Use have not been agreed to.")));
    }

}
