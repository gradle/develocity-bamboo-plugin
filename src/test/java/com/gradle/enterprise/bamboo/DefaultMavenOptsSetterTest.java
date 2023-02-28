package com.gradle.enterprise.bamboo;

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
import static org.hamcrest.Matchers.is;

public class DefaultMavenOptsSetterTest {

    private final GradleEnterpriseMavenOptsSetter mavenOptsSetter = new DefaultMavenOptsSetter(
        new EnvironmentVariableAccessorImpl(null, null)
    );

    @Test
    void orderIsSet() {
        assertThat(mavenOptsSetter.getOrder(), is(equalTo(Ordered.LOWEST_PRECEDENCE)));
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
            configuration.get(DefaultMavenOptsSetter.ENVIRONMENT_VARIABLES_KEY),
            is(equalTo("MAVEN_OPTS=\"-Dmaven.ext.class.path=test/path/gradle-enterprise-maven-extension-1.15.4.jar -Dgradle.scan.uploadInBackground=false -Dgradle.enterprise.url=url\""))
        );
    }

}
