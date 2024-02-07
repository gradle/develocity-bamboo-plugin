package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import org.springframework.core.Ordered;

public interface EnvironmentVariableSetter extends Ordered {

    boolean applies(RuntimeTaskDefinition task);

    void apply(RuntimeTaskDefinition task, String name, String value);
}
