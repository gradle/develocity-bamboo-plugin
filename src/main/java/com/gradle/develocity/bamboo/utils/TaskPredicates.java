package com.gradle.develocity.bamboo.utils;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.gradle.develocity.bamboo.GradleBuildScanInjector;
import com.gradle.develocity.bamboo.MavenBuildScanInjector;

import java.util.function.Predicate;

import static com.gradle.develocity.bamboo.utils.StringPredicates.endsWith;

public final class TaskPredicates {

    private TaskPredicates() {
    }

    public static Predicate<RuntimeTaskDefinition> artifactoryMaven3TaskTester() {
        return new PluginKeyTester(endsWith(MavenBuildScanInjector.ARTIFACTORY_MAVEN_3_TASK_KEY_SUFFIX));
    }

    public static Predicate<RuntimeTaskDefinition> artifactoryGradleTaskTester() {
        return new PluginKeyTester(endsWith(GradleBuildScanInjector.ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX));
    }

    private static final class PluginKeyTester implements Predicate<RuntimeTaskDefinition> {

        private final Predicate<String> pluginKeyPredicate;

        private PluginKeyTester(Predicate<String> pluginKeyPredicate) {
            this.pluginKeyPredicate = pluginKeyPredicate;
        }

        @Override
        public boolean test(RuntimeTaskDefinition task) {
            return pluginKeyPredicate.test(task.getPluginKey());
        }
    }
}
