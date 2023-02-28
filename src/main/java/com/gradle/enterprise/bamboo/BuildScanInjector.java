package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.gradle.enterprise.bamboo.config.PersistentConfiguration;
import com.gradle.enterprise.bamboo.config.BuildToolConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

public interface BuildScanInjector<C extends BuildToolConfiguration> {

    default Collection<RuntimeTaskDefinition> getSupportedTasks(BuildContext buildContext) {
        return buildContext.getRuntimeTaskDefinitions()
            .stream()
            .filter(task -> task.isEnabled() && isSupported(task))
            .collect(Collectors.toList());
    }

    default boolean hasSupportedTasks(BuildContext buildContext) {
        return !getSupportedTasks(buildContext).isEmpty();
    }

    boolean isSupported(RuntimeTaskDefinition task);

    C buildToolConfiguration(PersistentConfiguration configuration);

    BuildTool buildTool();

    void inject(BuildContext context);
}
