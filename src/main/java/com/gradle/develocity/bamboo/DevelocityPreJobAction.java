package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.chains.StageExecution;
import com.atlassian.bamboo.chains.plugins.PreJobAction;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.gradle.develocity.bamboo.config.BuildToolConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.config.UsernameAndPassword;
import com.gradle.develocity.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DevelocityPreJobAction implements PreJobAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DevelocityPreJobAction.class);

    private final PersistentConfigurationManager configurationManager;
    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final List<BuildScanInjector<? extends BuildToolConfiguration>> injectors;
    private final ShortLivedTokenClient shortLivedTokenClient;

    public DevelocityPreJobAction(
            PersistentConfigurationManager configurationManager,
            UsernameAndPasswordCredentialsProvider credentialsProvider,
            List<BuildScanInjector<? extends BuildToolConfiguration>> injectors,
            ShortLivedTokenClient shortLivedTokenClient
    ) {
        this.configurationManager = configurationManager;
        this.credentialsProvider = credentialsProvider;
        this.injectors = injectors;
        this.shortLivedTokenClient = shortLivedTokenClient;
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
                    sharedCredentialName, Constants.DEVELOCITY_ACCESS_KEY
            );
            return;
        }

        // Access key is stored in a password field
        String accessKey = credentials.getPassword();
        if (StringUtils.isBlank(accessKey)) {
            LOGGER.warn(
                    "Shared credentials with the name {} do not have password set. Environment variable {} will not be set",
                    sharedCredentialName, Constants.DEVELOCITY_ACCESS_KEY
            );
            return;
        }

        DevelocityAccessCredentials allKeys = DevelocityAccessCredentials.parse(accessKey);
        if (allKeys.isEmpty()) {
            LOGGER.warn(
                    "Cannot parse access keys from {} shared credential. Environment variable {} will not be set",
                    sharedCredentialName, Constants.DEVELOCITY_ACCESS_KEY
            );
            return;
        }

        boolean isInjectionEnabled = injectors.stream()
                .filter(i -> i.hasSupportedTasks(buildContext))
                .map(i -> i.buildToolConfiguration(configuration))
                .anyMatch(BuildToolConfiguration::isEnabled);

        if (isInjectionEnabled) {
            // If we know the URL or there's only one access key configured corresponding to the right URL
            if (allKeys.isSingleKey() || configuration.isEnforceUrl()) {
                String hostnameFromServerUrl = getHostnameFromServerUrl(configuration.getServer());
                if (hostnameFromServerUrl == null) {
                    LOGGER.warn("Could not extract hostname from Develocity server URL");
                    return;
                }

                allKeys.find(hostnameFromServerUrl)
                        .flatMap(parsedKey -> shortLivedTokenClient.get(configuration.getServer(), parsedKey, configuration.getShortLivedTokenExpiry()))
                        .ifPresent(shortLivedToken -> buildContext.getVariableContext().addLocalVariable(Constants.ACCESS_KEY, shortLivedToken.getRaw()));
            } else {
                // We're not sure exactly which Develocity URL will be effectively used so as best effort:
                // let's translate all access keys to short-lived tokens
                List<DevelocityAccessCredentials.HostnameAccessKey> shortLivedTokens = allKeys.stream()
                        .map(key -> shortLivedTokenClient.get("https://" + key.getHostname(), key, configuration.getShortLivedTokenExpiry()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                if (!shortLivedTokens.isEmpty()) {
                    buildContext.getVariableContext().addLocalVariable(Constants.ACCESS_KEY, DevelocityAccessCredentials.of(shortLivedTokens).getRaw());
                }
            }
        }
    }

    private static String getHostnameFromServerUrl(String serverUrl) {
        try {
            return new URL(serverUrl).getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
