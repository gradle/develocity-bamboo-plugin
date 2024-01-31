package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.util.BambooIterables;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.gradle.develocity.bamboo.utils.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class DevelocityAccessKeyExporter {

    private final List<EnvironmentVariableSetter> environmentVariableSetters;

    @Autowired
    public DevelocityAccessKeyExporter(List<EnvironmentVariableSetter> environmentVariableSetters) {
        this.environmentVariableSetters = Collections.sortedByOrder(environmentVariableSetters);
    }

    public void exportDevelocityAccessKey(BuildContext buildContext,
                                          Collection<RuntimeTaskDefinition> tasks) {
        BambooIterables.stream(buildContext.getVariableContext().getPasswordVariables())
            .filter(this::isDevelocityAccessKey)
            .findFirst()
            .map(VariableDefinitionContext::getValue)
            .ifPresent(accessKey ->
                tasks.forEach(task ->
                    environmentVariableSetters
                        .stream()
                        .filter(setter -> setter.applies(task))
                        .findFirst()
                        .ifPresent(setter -> setter.apply(task, Constants.DEVELOCITY_ACCESS_KEY, accessKey))));
    }

    private boolean isDevelocityAccessKey(VariableDefinitionContext context) {
        return Constants.ACCESS_KEY.equals(context.getKey());
    }
}
