package it.com.gradle.develocity.bamboo.injection;

import com.atlassian.bamboo.builder.LifeCycleState;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.gradle.develocity.bamboo.Constants;
import com.gradle.develocity.bamboo.model.BuildResultDetails;
import com.gradle.develocity.bamboo.model.JobKey;
import com.gradle.develocity.bamboo.model.TestUser;
import com.gradle.develocity.bamboo.model.TriggeredBuild;
import it.com.gradle.develocity.bamboo.BrowserTest;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

abstract class AbstractInjectionTest extends BrowserTest {

    public static final String PROJECT_KEY = "TP";

    @BeforeEach
    void login() {
        loginAs(TestUser.ADMIN);
        ensurePluginConfiguration(form -> form.setVcsRepositoryFilter("").doNotEnforceUrl());
    }

    public final PlanResultKey triggerBuild(PlanKey planKey, JobKey jobKey) {
        TriggeredBuild triggerBuildResponse = bambooApi.triggerBuild(planKey);
        return PlanKeys.getPlanResultKey(jobKey.getValue(), triggerBuildResponse.getBuildNumber());
    }

    public final void waitForBuildToFinish(PlanResultKey planResultKey) {
        await()
            .pollDelay(Duration.ofSeconds(1))
            .atMost(Duration.ofMinutes(5))
            .pollInterval(FibonacciPollInterval.fibonacci(TimeUnit.SECONDS))
            .until(buildIsFinished(planResultKey));
    }

    private static Callable<Boolean> buildIsFinished(PlanResultKey planResultKey) {
        return () -> {
            BuildResultDetails resultDetails = bambooApi.getBuildResultDetails(planResultKey);
            LifeCycleState lifeCycleState = LifeCycleState.getInstance(resultDetails.getLifeCycleState());

            if (LifeCycleState.isNotBuilt(lifeCycleState)) {
                throw new IllegalStateException("Unexpected build " + planResultKey.getKey() + " state " + lifeCycleState);
            }

            return LifeCycleState.isFinished(lifeCycleState);
        };
    }

    public final Optional<String> getBuildScansFromMetadata(PlanResultKey planResultKey) {
        BuildResultDetails resultDetails = bambooApi.getBuildResultDetails(planResultKey);

        return resultDetails.getMetadata()
            .getItems()
            .stream()
            .filter(metadataItem -> metadataItem.getKey().equals(Constants.BUILD_SCANS_KEY))
            .findFirst()
            .map(BuildResultDetails.Metadata.Item::getValue);
    }
}
