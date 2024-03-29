package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.CustomPreBuildAction;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.utils.error.SimpleErrorCollection;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.task.AbstractBuildTask;
import com.atlassian.bamboo.ww2.actions.build.admin.create.BuildConfiguration;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.gradle.develocity.bamboo.config.BuildToolConfiguration;
import com.gradle.develocity.bamboo.config.GradleConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DevelocityPreBuildAction extends AbstractBuildTask implements CustomPreBuildAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevelocityPreBuildAction.class);

    private final List<BuildScanInjector<? extends BuildToolConfiguration>> injectors;
    private final BuildLoggerManager buildLoggerManager;
    private final PersistentConfigurationManager configurationManager;

    public DevelocityPreBuildAction(
        List<BuildScanInjector<? extends BuildToolConfiguration>> injectors,
        @ComponentImport BuildLoggerManager buildLoggerManager,
        PersistentConfigurationManager configurationManager
    ) {
        this.injectors = injectors;
        this.buildLoggerManager = buildLoggerManager;
        this.configurationManager = configurationManager;
    }

    @Override
    public ErrorCollection validate(BuildConfiguration configuration) {
        return new SimpleErrorCollection();
    }

    @NotNull
    @Override
    public BuildContext call() {
        if (injectionIsAllowedOnVcsRepo()) {
            for (BuildScanInjector<? extends BuildToolConfiguration> injector : injectors) {
                try {
                    injector.inject(buildContext);
                } catch (Exception e) {
                    LOGGER.error("Develocity {} auto-injection failed", injector.buildTool().displayName(), e);

                    addErrorToBuildLog(injector, e);
                }
            }
        }

        return buildContext;
    }

    private boolean injectionIsAllowedOnVcsRepo() {
        // TODO: we don't need GradleConfiguration
        //  need to remove inheritance and use composition for BuildToolConfiguration, and instantiate that here
        return configurationManager.load().map(GradleConfiguration::of).map(c -> c.vcsRepositoryFilter).map(f -> {
            for (String url : vcsRepoUrls()) {
                switch (f.matches(url)) {
                    case EXCLUDED:
                        return false;
                    case INCLUDED:
                        return true;
                }
            }
            return false;
        }).orElse(true);
    }

    private Set<String> vcsRepoUrls() {
        return buildContext.getRelevantRepositoryIds().stream().map(id -> buildContext.getVcsRepositoryMap().get(id))
            .map(r -> r.getVcsLocation().getConfiguration())
            .flatMap(c -> c.keySet().stream().map(k -> {
                if (k.contains("repositoryUrl")) {
                    return c.get(k);
                } else if (k.contains("github.repository")) {
                    return "https://github.com/" + c.get(k);
                } else {
                    return null;
                }
            }))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private void addErrorToBuildLog(BuildScanInjector injector, Exception ex) {
        try {
            String message = String.format("Develocity %s auto-injection failed", injector.buildTool().displayName());

            buildLoggerManager.getLogger(buildContext.getPlanResultKey())
                .addErrorLogEntry(message, ex);
        } catch (Exception e) {
            LOGGER.error("Unable to add error log entry", e);
        }
    }

}
