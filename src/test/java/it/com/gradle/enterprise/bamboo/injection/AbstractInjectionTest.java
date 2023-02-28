package it.com.gradle.enterprise.bamboo.injection;

import com.atlassian.bamboo.builder.LifeCycleState;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.gradle.enterprise.bamboo.BambooApi;
import com.gradle.enterprise.bamboo.Constants;
import com.gradle.enterprise.bamboo.model.BuildResultDetails;
import com.gradle.enterprise.bamboo.model.JobKey;
import com.gradle.enterprise.bamboo.model.TestUser;
import com.gradle.enterprise.bamboo.model.TriggeredBuild;
import it.com.gradle.enterprise.bamboo.BrowserTest;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

abstract class AbstractInjectionTest extends BrowserTest {

    public static final String PROJECT_KEY = "TP";

    protected static BambooApi bambooApi;

    @BeforeAll
    static void createBambooApi() {
        bambooApi = new BambooApi(BAMBOO, TestUser.ADMIN);
    }

    @AfterAll
    static void tearDownBambooApi() {
        bambooApi.close();
    }

    @BeforeEach
    void login() {
        loginAs(TestUser.ADMIN);
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
