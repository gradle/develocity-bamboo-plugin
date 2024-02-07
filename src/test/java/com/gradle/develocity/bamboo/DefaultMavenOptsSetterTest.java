package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.process.EnvironmentVariableAccessorImpl;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskDefinitionImpl;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinitionImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class DefaultMavenOptsSetterTest {

    private final DevelocityMavenOptsSetter mavenOptsSetter =
        new DefaultMavenOptsSetter(new EnvironmentVariableAccessorImpl(null, null));

    @Test
    void orderIsSet() {
        assertThat(mavenOptsSetter.getOrder(), is(equalTo(Ordered.LOWEST_PRECEDENCE)));
    }

    @Test
    void mavenOpsAreSet() {
        // given
        List<SystemProperty> systemProperties = new ArrayList<>();
        systemProperties.add(new SystemProperty("maven.ext.class.path", "test/path/gradle-enterprise-maven-extension-1.15.4.jar"));
        systemProperties.add(new SystemProperty("gradle.scan.uploadInBackground", "false"));
        systemProperties.add(new SystemProperty("gradle.enterprise.url", "url"));

        TaskDefinition taskDefinition =
            new TaskDefinitionImpl(
                RandomUtils.nextLong(),
                RandomStringUtils.randomAscii(10),
                null,
                Collections.singletonMap("key", "value")
            );
        RuntimeTaskDefinition runtimeTaskDefinition = new RuntimeTaskDefinitionImpl(taskDefinition);

        // when
        mavenOptsSetter.apply(runtimeTaskDefinition, systemProperties);

        // then
        Map<String, String> configuration = runtimeTaskDefinition.getConfiguration();

        assertThat(configuration.size(), is(equalTo(2)));
        assertThat(configuration, hasEntry(equalTo("key"), equalTo("value")));
        assertThat(configuration,
            hasEntry(
                equalTo(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY),
                equalTo("MAVEN_OPTS=\"-Dmaven.ext.class.path=test/path/gradle-enterprise-maven-extension-1.15.4.jar -Dgradle.scan.uploadInBackground=false -Dgradle.enterprise.url=url\""))
        );
    }

}
