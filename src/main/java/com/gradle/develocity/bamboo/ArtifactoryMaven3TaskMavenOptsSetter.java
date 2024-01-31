package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.google.common.annotations.VisibleForTesting;
import com.gradle.develocity.bamboo.utils.TaskPredicates;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.gradle.develocity.bamboo.DevelocityMavenOptsSetter.mergeMavenOpts;

@Component
public class ArtifactoryMaven3TaskMavenOptsSetter implements DevelocityMavenOptsSetter {

    // https://github.com/jfrog/bamboo-artifactory-plugin/blob/master/src/main/java/org/jfrog/bamboo/context/Maven3BuildContext.java#L61
    @VisibleForTesting
    static final String MAVEN_OPTS_KEY = "builder.artifactoryMaven3Builder.mavenOpts";

    private static final Predicate<RuntimeTaskDefinition> ARTIFACTORY_MAVEN_3_TASK_TESTER = TaskPredicates.artifactoryMaven3TaskTester();

    @Override
    public boolean applies(RuntimeTaskDefinition task) {
        return ARTIFACTORY_MAVEN_3_TASK_TESTER.test(task);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void apply(RuntimeTaskDefinition task, List<SystemProperty> systemProperties) {
        Map<String, String> configuration = task.getConfiguration();

        String existingMavenOpts = configuration.get(MAVEN_OPTS_KEY);
        String updatedMavenOpts = mergeMavenOpts(existingMavenOpts, systemProperties);

        configuration.put(MAVEN_OPTS_KEY, updatedMavenOpts);
    }
}
