package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.gradle.develocity.bamboo.utils.EnvironmentVariables;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

abstract class AbstractEnvironmentVariableSetter implements EnvironmentVariableSetter {

    private final String environmentVariablesKey;

    AbstractEnvironmentVariableSetter(String environmentVariablesKey) {
        this.environmentVariablesKey = environmentVariablesKey;
    }

    @Override
    public void apply(RuntimeTaskDefinition task, String name, String value) {
        Map<String, String> configuration = task.getConfiguration();

        String currentEnvironment = StringUtils.trimToNull(configuration.get(environmentVariablesKey));
        String additionalEnvironmentVariable = String.format("%s=%s", name, EnvironmentVariables.quoteIfNeeded(value));

        String updatedEnvironment =
            (currentEnvironment == null)
                ? additionalEnvironmentVariable
                : StringUtils.join(new String[]{currentEnvironment, additionalEnvironmentVariable}, ' ');

        configuration.put(environmentVariablesKey, updatedEnvironment);
    }
}
