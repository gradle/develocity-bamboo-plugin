package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.gradle.develocity.bamboo.utils.TaskPredicates;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class ArtifactoryGradleEnvironmentVariableSetter extends AbstractEnvironmentVariableSetter {

    private static final String ENVIRONMENT_VARIABLES_KEY = "builder.artifactoryGradleBuilder.environmentVariables";

    private static final Predicate<RuntimeTaskDefinition> ARTIFACTORY_GRADLE_TASK_TESTER = TaskPredicates.artifactoryGradleTaskTester();

    public ArtifactoryGradleEnvironmentVariableSetter() {
        super(ENVIRONMENT_VARIABLES_KEY);
    }

    @Override
    public boolean applies(RuntimeTaskDefinition task) {
        return ARTIFACTORY_GRADLE_TASK_TESTER.test(task);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
