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

import static com.gradle.develocity.bamboo.SystemProperty.SystemPropertyKeyWithDeprecatedKey.SERVER_URL_SYSTEM_PROPERTIES;
import static com.gradle.develocity.bamboo.SystemProperty.SystemPropertyKeyWithDeprecatedKey.UPLOAD_IN_BACKGROUND_SYSTEM_PROPERTIES;
import static com.gradle.develocity.bamboo.SystemProperty.SimpleSystemPropertyKey.MAVEN_EXT_CLASS_PATH_SYSTEM_PROPERTY;
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
        systemProperties.add(MAVEN_EXT_CLASS_PATH_SYSTEM_PROPERTY.forValue("test/path/develocity-maven-extension-1.21.jar"));
        systemProperties.addAll(UPLOAD_IN_BACKGROUND_SYSTEM_PROPERTIES.forValue(false));
        systemProperties.addAll(SERVER_URL_SYSTEM_PROPERTIES.forValue("url"));

        TaskDefinition taskDefinition = new TaskDefinitionImpl(
            RandomUtils.nextLong(), RandomStringUtils.randomAscii(10), null, Collections.singletonMap("key", "value")
        );

        RuntimeTaskDefinition runtimeTaskDefinition = new RuntimeTaskDefinitionImpl(taskDefinition);

        mavenOptsSetter.apply(runtimeTaskDefinition, systemProperties);

        Map<String, String> configuration = runtimeTaskDefinition.getConfiguration();

        assertThat(configuration.get("key"), is(equalTo("value")));
        assertThat(
            configuration.get(ArtifactoryMaven3TaskMavenOptsSetter.MAVEN_OPTS_KEY),
            is(equalTo(
                    "-Dmaven.ext.class.path=test/path/develocity-maven-extension-1.21.jar " +
                            "-Ddevelocity.scan.uploadInBackground=false " +
                            "-Dgradle.scan.uploadInBackground=false " +
                            "-Ddevelocity.url=url " +
                            "-Dgradle.enterprise.url=url"
            ))
        );
    }

}
