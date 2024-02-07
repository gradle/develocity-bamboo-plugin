package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskDefinitionImpl;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinitionImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ArtifactoryMaven3TaskMavenOptsSetterTest {

    private final DevelocityMavenOptsSetter mavenOptsSetter = new ArtifactoryMaven3TaskMavenOptsSetter();

    @Test
    void orderIsSet() {
        assertThat(mavenOptsSetter.getOrder(), is(equalTo(0)));
    }

    @Test
    void mavenOpsAreSet() {
        List<SystemProperty> systemProperties = new ArrayList<>();
        systemProperties.add(new SystemProperty("maven.ext.class.path", "test/path/gradle-enterprise-maven-extension-1.15.4.jar"));
        systemProperties.add(new SystemProperty("gradle.scan.uploadInBackground", "false"));
        systemProperties.add(new SystemProperty("gradle.enterprise.url", "url"));

        TaskDefinition taskDefinition = new TaskDefinitionImpl(
            RandomUtils.nextLong(), RandomStringUtils.randomAscii(10), null, Collections.singletonMap("key", "value")
        );

        RuntimeTaskDefinition runtimeTaskDefinition = new RuntimeTaskDefinitionImpl(taskDefinition);

        mavenOptsSetter.apply(runtimeTaskDefinition, systemProperties);

        Map<String, String> configuration = runtimeTaskDefinition.getConfiguration();

        assertThat(configuration.get("key"), is(equalTo("value")));
        assertThat(
            configuration.get(ArtifactoryMaven3TaskMavenOptsSetter.MAVEN_OPTS_KEY),
            is(equalTo("-Dmaven.ext.class.path=test/path/gradle-enterprise-maven-extension-1.15.4.jar -Dgradle.scan.uploadInBackground=false -Dgradle.enterprise.url=url"))
        );
    }

}
