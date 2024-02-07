package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.gradle.develocity.bamboo.utils.EnvironmentVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gradle.develocity.bamboo.DevelocityMavenOptsSetter.mergeMavenOpts;

@Component
public class DefaultMavenOptsSetter implements DevelocityMavenOptsSetter {

    private static final String MAVEN_OPTS = "MAVEN_OPTS";

    private final EnvironmentVariableAccessor environmentVariableAccessor;

    @Autowired
    public DefaultMavenOptsSetter(@ComponentImport EnvironmentVariableAccessor environmentVariableAccessor) {
        this.environmentVariableAccessor = environmentVariableAccessor;
    }

    @Override
    public boolean applies(RuntimeTaskDefinition task) {
        return true;
    }

    @Override
    public int getOrder() {
        // needs to be the last one
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void apply(RuntimeTaskDefinition task, List<SystemProperty> systemProperties) {
        Map<String, String> configuration = task.getConfiguration();

        String currentEnvironment = configuration.get(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY);

        Map<String, String> environment =
            new HashMap<>(environmentVariableAccessor.splitEnvironmentAssignments(currentEnvironment));

        String existingMavenOpts = environment.remove(MAVEN_OPTS);
        environment.put(MAVEN_OPTS, mergeMavenOpts(existingMavenOpts, systemProperties));

        // splitEnvironmentAssignments method internally unquotes values before placing them into the Map,
        // therefore we need to quote values again before joining them back into the String
        String updatedEnvironment =
            environmentVariableAccessor.joinEnvironmentVariables(quoteValuesIfNeeded(environment));

        configuration.put(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY, updatedEnvironment);
    }

    private Map<String, String> quoteValuesIfNeeded(Map<String, String> map) {
        return map
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, EnvironmentVariables::quoteValueIfNeeded));
    }
}
