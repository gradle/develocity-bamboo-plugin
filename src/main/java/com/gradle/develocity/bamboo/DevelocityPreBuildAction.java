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
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.gradle.develocity.bamboo.Utils.vcsRepoUrls;

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
        // Loading a GradleConfiguration is not needed, we can refactor this later to load some general settings instead
        return configurationManager.load().map(GradleConfiguration::of)
            .map(c -> c.vcsRepositoryFilter)
            .filter(f -> StringUtils.isNotBlank(f.getVcsRepositoryFilter()))
            .map(f -> {
            for (String url : vcsRepoUrls(buildContext)) {
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
