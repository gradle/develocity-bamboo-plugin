package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import org.springframework.stereotype.Component;

@Component
public class DefaultEnvironmentVariableSetter extends AbstractEnvironmentVariableSetter {

    private static final String ENVIRONMENT_VARIABLES_KEY = "environmentVariables";

    public DefaultEnvironmentVariableSetter() {
        super(ENVIRONMENT_VARIABLES_KEY);
    }

    @Override
    public boolean applies(RuntimeTaskDefinition task) {
        return true;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
