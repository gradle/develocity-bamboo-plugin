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
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        if (VcsRepositoryUtils.injectionIsAllowedOnVcsRepo(configurationManager, buildContext)) {
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
