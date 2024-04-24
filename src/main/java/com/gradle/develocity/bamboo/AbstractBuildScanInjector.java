package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentResult;
import com.gradle.develocity.bamboo.config.BuildToolConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

abstract class AbstractBuildScanInjector<C extends BuildToolConfiguration> implements BuildScanInjector<C> {

    private final BuildLoggerManager buildLoggerManager;
    private final PersistentConfigurationManager configurationManager;

    protected AbstractBuildScanInjector(BuildLoggerManager buildLoggerManager,
                                        PersistentConfigurationManager configurationManager) {
        this.buildLoggerManager = buildLoggerManager;
        this.configurationManager = configurationManager;
    }

    protected final Optional<C> loadConfiguration() {
        return configurationManager.load().map(this::buildToolConfiguration);
    }

    protected final void setupBuildScansLogInterceptor(BuildContext buildContext) {
        BuildLogger buildLogger = buildLoggerManager.getLogger(buildContext.getPlanResultKey());
        buildLogger
            .getInterceptorStack()
            .addForJob(new BuildScanLogScanner(new BuildScanCollector(buildContext)));
    }

    protected final void registerDevelocityResources(BuildContext buildContext, File... resources) {
        CurrentResult currentResult = buildContext.getCurrentResult();
        if (currentResult != null) {
            Arrays.stream(resources)
                .map(File::getAbsolutePath)
                .forEach(resource ->
                    currentResult.getCustomBuildData()
                        .merge(Constants.DEVELOCITY_RESOURCES_KEY, resource, this::concat));
        }
    }

    private String concat(String existingResources, String resource) {
        return String.join(Constants.DEVELOCITY_RESOURCES_SEPARATOR, existingResources, resource);
    }

    protected static boolean anyMatch(Set<Predicate<String>> predicates, RuntimeTaskDefinition task) {
        String pluginKey = task.getPluginKey();
        return predicates.stream().anyMatch(predicate -> predicate.test(pluginKey));
    }
}
