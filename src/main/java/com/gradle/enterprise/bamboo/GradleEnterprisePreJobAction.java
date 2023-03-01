package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.gradle.enterprise.bamboo.config.BuildToolConfiguration;
import com.gradle.enterprise.bamboo.config.PersistentConfiguration;
import com.gradle.enterprise.bamboo.config.PersistentConfigurationManager;
import com.gradle.enterprise.bamboo.config.UsernameAndPassword;
import com.gradle.enterprise.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GradleEnterprisePreJobAction implements PreJobAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleEnterprisePreJobAction.class);

    private final PersistentConfigurationManager configurationManager;
    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final List<BuildScanInjector<? extends BuildToolConfiguration>> injectors;

    public GradleEnterprisePreJobAction(PersistentConfigurationManager configurationManager,
                                        UsernameAndPasswordCredentialsProvider credentialsProvider,
                                        List<BuildScanInjector<? extends BuildToolConfiguration>> injectors) {
        this.configurationManager = configurationManager;
        this.credentialsProvider = credentialsProvider;
        this.injectors = injectors;
    }

    @Override
    public void execute(@NotNull StageExecution stageExecution, @NotNull BuildContext buildContext) {
        PersistentConfiguration configuration = configurationManager.load().orElse(null);
        if (configuration == null) {
            return;
        }

        String sharedCredentialName = configuration.getSharedCredentialName();
        if (StringUtils.isBlank(sharedCredentialName)) {
            return;
        }

        UsernameAndPassword credentials = credentialsProvider.findByName(sharedCredentialName).orElse(null);
        if (credentials == null) {
            LOGGER.warn(
                "Shared credentials with the name {} are not found. Environment variable {} will not be set",
                sharedCredentialName, Constants.GRADLE_ENTERPRISE_ACCESS_KEY
            );
            return;
        }

        // Access key is stored in a password field
        String accessKey = credentials.getPassword();
        if (StringUtils.isBlank(accessKey)) {
            LOGGER.warn(
                "Shared credentials with the name {} do not have password set. Environment variable {} will not be set",
                sharedCredentialName, Constants.GRADLE_ENTERPRISE_ACCESS_KEY
            );
            return;
        }

        injectors.stream()
            .filter(i -> i.hasSupportedTasks(buildContext))
            .map(i -> i.buildToolConfiguration(configuration))
            .filter(BuildToolConfiguration::isEnabled)
            .findFirst()
            .ifPresent(__ ->
                buildContext
                    .getVariableContext()
                    .addLocalVariable(Constants.ACCESS_KEY, accessKey));
    }
}
