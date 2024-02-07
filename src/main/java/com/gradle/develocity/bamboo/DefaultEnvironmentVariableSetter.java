package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import org.springframework.stereotype.Component;

@Component
public class DefaultEnvironmentVariableSetter extends AbstractEnvironmentVariableSetter {

    public DefaultEnvironmentVariableSetter() {
        super(Constants.DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY);
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
}
