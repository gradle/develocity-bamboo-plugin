package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.gradle.develocity.bamboo.utils.TaskPredicates;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class ArtifactoryMaven3EnvironmentVariableSetter extends AbstractEnvironmentVariableSetter {

    private static final String ENVIRONMENT_VARIABLES_KEY = "builder.artifactoryMaven3Builder.environmentVariables";

    private static final Predicate<RuntimeTaskDefinition> ARTIFACTORY_MAVEN_3_TASK_TESTER = TaskPredicates.artifactoryMaven3TaskTester();

    public ArtifactoryMaven3EnvironmentVariableSetter() {
        super(ENVIRONMENT_VARIABLES_KEY);
    }

    @Override
    public boolean applies(RuntimeTaskDefinition task) {
        return ARTIFACTORY_MAVEN_3_TASK_TESTER.test(task);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
