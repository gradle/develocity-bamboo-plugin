package com.gradle.develocity.bamboo.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.config.UsernameAndPassword;
import com.gradle.develocity.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class BuildScansConfigAction extends GlobalAdminAction {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(-[-\\w]+)?$");

    /* Common parameters for all build systems */
    private String server;
    private boolean allowUntrustedServer;
    private String sharedCredentialName;

    /* Gradle specific parameters */
    private String develocityPluginVersion;
    private String ccudPluginVersion;
    private String pluginRepository;

    /* Maven specific parameters */
    private boolean injectMavenExtension;
    private boolean injectCcudExtension;

    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final PersistentConfigurationManager configurationManager;

    public BuildScansConfigAction(UsernameAndPasswordCredentialsProvider credentialsProvider,
                                  PersistentConfigurationManager configurationManager) {
        this.credentialsProvider = credentialsProvider;
        this.configurationManager = configurationManager;
    }

    public String input() {
        configurationManager.load()
            .ifPresent(config -> {
                server = config.getServer();
                allowUntrustedServer = config.isAllowUntrustedServer();
                sharedCredentialName = config.getSharedCredentialName();
                develocityPluginVersion = config.getGePluginVersion();
                ccudPluginVersion = config.getCcudPluginVersion();
                pluginRepository = config.getPluginRepository();
                injectMavenExtension = config.isInjectMavenExtension();
                injectCcudExtension = config.isInjectCcudExtension();
            });

        return INPUT;
    }

    @Override
    public void validate() {
        clearErrorsAndMessages();

        if (!isBlankOrValidUrl(server)) {
            addFieldError("server", "Please specify a valid URL of the Develocity server.");
        }

        if (StringUtils.isNotBlank(sharedCredentialName)) {
            UsernameAndPassword credentials = credentialsProvider.findByName(sharedCredentialName).orElse(null);
            if (credentials == null) {
                addFieldError("sharedCredentialName", "Please specify the name of the existing shared credential of type 'Username and password'.");
            } else {
                String accessKey = credentials.getPassword();
                if (!AccessKeyValidator.isValid(accessKey)) {
                    addFieldError("sharedCredentialName", "Shared credential contains an invalid access key.");
                }
            }
        }

        if (!isBlankOrValidVersion(develocityPluginVersion)) {
            addFieldError("develocityPluginVersion", "Please specify a valid version of the Develocity Gradle plugin.");
        }

        if (!isBlankOrValidVersion(ccudPluginVersion)) {
            addFieldError("ccudPluginVersion", "Please specify a valid version of the Common Custom User Data Gradle plugin.");
        }

        if (!isBlankOrValidUrl(pluginRepository)) {
            addFieldError("pluginRepository", "Please specify a valid URL of the Gradle plugins repository.");
        }
    }

    private static boolean isBlankOrValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return true;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static boolean isBlankOrValidVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return true;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }

    public String save() {
        configurationManager.save(
            new PersistentConfiguration()
                .setServer(server)
                .setAllowUntrustedServer(allowUntrustedServer)
                .setSharedCredentialName(sharedCredentialName)
                .setPluginRepository(pluginRepository)
                .setGePluginVersion(develocityPluginVersion)
                .setCcudPluginVersion(ccudPluginVersion)
                .setInjectMavenExtension(injectMavenExtension)
                .setInjectCcudExtension(injectCcudExtension));

        return SUCCESS;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isAllowUntrustedServer() {
        return allowUntrustedServer;
    }

    public void setAllowUntrustedServer(boolean allowUntrustedServer) {
        this.allowUntrustedServer = allowUntrustedServer;
    }

    public String getSharedCredentialName() {
        return sharedCredentialName;
    }

    public void setSharedCredentialName(String sharedCredentialName) {
        this.sharedCredentialName = sharedCredentialName;
    }

    public String getDevelocityPluginVersion() {
        return develocityPluginVersion;
    }

    public void setDevelocityPluginVersion(String develocityPluginVersion) {
        this.develocityPluginVersion = develocityPluginVersion;
    }

    public String getCcudPluginVersion() {
        return ccudPluginVersion;
    }

    public void setCcudPluginVersion(String ccudPluginVersion) {
        this.ccudPluginVersion = ccudPluginVersion;
    }

    public String getPluginRepository() {
        return pluginRepository;
    }

    public void setPluginRepository(String pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public boolean isInjectMavenExtension() {
        return injectMavenExtension;
    }

    public void setInjectMavenExtension(boolean injectMavenExtension) {
        this.injectMavenExtension = injectMavenExtension;
    }

    public boolean isInjectCcudExtension() {
        return injectCcudExtension;
    }

    public void setInjectCcudExtension(boolean injectCcudExtension) {
        this.injectCcudExtension = injectCcudExtension;
    }
}
