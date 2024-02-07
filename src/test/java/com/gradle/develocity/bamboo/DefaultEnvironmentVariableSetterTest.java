package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.task.TaskDefinitionImpl;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinitionImpl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.Ordered;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

class DefaultEnvironmentVariableSetterTest {

    private final EnvironmentVariableSetter environmentVariableSetter = new DefaultEnvironmentVariableSetter();

    @Test
    void orderIsSet() {
        // expect
        assertThat(environmentVariableSetter.getOrder(), is(equalTo(Ordered.LOWEST_PRECEDENCE)));
    }

    @Test
    void addsNewEnvironmentVariable() {
        // given
        TaskDefinition taskDefinition =
            new TaskDefinitionImpl(
                RandomUtils.nextLong(),
                RandomStringUtils.randomAscii(10),
                null,
                Collections.emptyMap()
            );
        RuntimeTaskDefinition runtimeTaskDefinition = new RuntimeTaskDefinitionImpl(taskDefinition);

        // when
        environmentVariableSetter.apply(runtimeTaskDefinition, "NEW", "value");

        // then
        Map<String, String> result = runtimeTaskDefinition.getConfiguration();

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result,
            hasEntry(
                equalTo(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY),
                equalTo("NEW=value"))
        );
    }

    @ParameterizedTest
    @CsvSource({
        "NEW, value, OLD=value NEW=value",
        "NEW, value with space, OLD=value NEW=\"value with space\"",
        "NEW, \"value\" with space, OLD=value NEW='\"value\" with space'"
    })
    void updatesExistingEnvironmentVariable(String key, String value, String expected) {
        // given
        TaskDefinition taskDefinition =
            new TaskDefinitionImpl(
                RandomUtils.nextLong(),
                RandomStringUtils.randomAscii(10),
                null,
                Collections.singletonMap(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY, "OLD=value")
            );
        RuntimeTaskDefinition runtimeTaskDefinition = new RuntimeTaskDefinitionImpl(taskDefinition);

        // when
        environmentVariableSetter.apply(runtimeTaskDefinition, key, value);

        // then
        Map<String, String> result = runtimeTaskDefinition.getConfiguration();

        assertThat(result.size(), is(equalTo(1)));
        assertThat(result,
            hasEntry(
                equalTo(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY),
                equalTo(expected))
        );
    }
}
