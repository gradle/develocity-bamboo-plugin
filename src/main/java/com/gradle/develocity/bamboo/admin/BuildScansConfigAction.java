package com.gradle.develocity.bamboo.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bamboo.repository.NameValuePair;
import com.gradle.develocity.bamboo.DevelocityAccessCredentials;
import com.gradle.develocity.bamboo.MavenCoordinates;
import com.gradle.develocity.bamboo.VcsRepositoryFilter;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.config.UsernameAndPassword;
import com.gradle.develocity.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BuildScansConfigAction extends GlobalAdminAction {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(-[-\\w]+)?$");
    private static final Pattern SHORT_LIVED_TOKEN_EXPIRY_PATTERN = Pattern.compile("^(?:[1-9]|1\\d?|2[0-4]?)$");

    /* Common parameters for all build systems */
    private String server;
    private boolean allowUntrustedServer;
    private String sharedCredentialName;
    private boolean enforceUrl;

    /* Gradle specific parameters */
    private String develocityPluginVersion;
    private String ccudPluginVersion;
    private String pluginRepository;
    private String pluginRepositoryCredentialName;
    private boolean gradleCaptureFileFingerprints;

    /* Maven specific parameters */
    private boolean injectMavenExtension;
    private boolean injectCcudExtension;
    private boolean mavenCaptureFileFingerprints;

    private String mavenExtensionCustomCoordinates;
    private String ccudExtensionCustomCoordinates;

    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final PersistentConfigurationManager configurationManager;

    /* General settings */
    private String vcsRepositoryFilter;
    private String shortLivedTokenExpiry;

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
                develocityPluginVersion = config.getDevelocityPluginVersion();
                ccudPluginVersion = config.getCcudPluginVersion();
                pluginRepository = config.getPluginRepository();
                pluginRepositoryCredentialName = config.getPluginRepositoryCredentialName();
                injectMavenExtension = config.isInjectMavenExtension();
                injectCcudExtension = config.isInjectCcudExtension();
                mavenExtensionCustomCoordinates = config.getMavenExtensionCustomCoordinates();
                ccudExtensionCustomCoordinates = config.getCcudExtensionCustomCoordinates();
                enforceUrl = config.isEnforceUrl();
                vcsRepositoryFilter = config.getVcsRepositoryFilter();
                gradleCaptureFileFingerprints = config.isGradleCaptureFileFingerprints();
                mavenCaptureFileFingerprints = config.isMavenCaptureFileFingerprints();
                shortLivedTokenExpiry = config.getShortLivedTokenExpiry();
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
                if (!DevelocityAccessCredentials.isValid(accessKey)) {
                    addFieldError("sharedCredentialName", "Shared credential contains an invalid access key.");
                }
            }
        }

        if (!isBlankOrValid(VERSION_PATTERN, develocityPluginVersion)) {
            addFieldError("develocityPluginVersion", "Please specify a valid version of the Develocity Gradle plugin.");
        }

        if (!isBlankOrValid(VERSION_PATTERN, ccudPluginVersion)) {
            addFieldError("ccudPluginVersion", "Please specify a valid version of the Common Custom User Data Gradle plugin.");
        }

        if (!isBlankOrValidUrl(pluginRepository)) {
            addFieldError("pluginRepository", "Please specify a valid URL of the Gradle plugins repository.");
        }

        if (StringUtils.isNotBlank(pluginRepositoryCredentialName)) {
            UsernameAndPassword credentials = credentialsProvider.findByName(pluginRepositoryCredentialName).orElse(null);
            if (credentials == null) {
                addFieldError("pluginRepositoryCredentialName", "Please specify the name of the existing repository credential of type 'Username and password'.");
            }
        }

        if (!isBlankOrValidGavc(mavenExtensionCustomCoordinates)) {
            addFieldError("mavenExtensionCustomCoordinates", "Please specify a valid Maven groupId:artifactId(:version).");
        }

        if (!isBlankOrValidGavc(ccudExtensionCustomCoordinates)) {
            addFieldError("ccudExtensionCustomCoordinates", "Please specify a valid Maven groupId:artifactId(:version).");
        }

        if (!isBlankOrValidVcsFilter(vcsRepositoryFilter)) {
            addFieldError("vcsRepositoryFilter", "Please specify a valid vcs filter, ie lines of: +|-:repository_matching_keyword");
        }

        if (!isBlankOrValid(SHORT_LIVED_TOKEN_EXPIRY_PATTERN, shortLivedTokenExpiry)) {
            addFieldError("shortLivedTokenExpiry", "Please specify a valid short-lived token expiry in hours between 1 and 24, i.e. 6");
        }
    }

    private boolean isBlankOrValidVcsFilter(String vcsRepositoryFilter) {
        if (StringUtils.isBlank(vcsRepositoryFilter)) {
            return true;
        }
        return !VcsRepositoryFilter.of(vcsRepositoryFilter).isEmpty();
    }

    public List<NameValuePair> getUsernameAndPasswordCredentialNames() {
        List<NameValuePair> usernameAndPasswordCredentials = credentialsProvider.getAllUsernameAndPasswordCredentials()
                .stream()
                .map(credentialName -> new NameValuePair(credentialName, credentialName))
                .collect(Collectors.toList());

        usernameAndPasswordCredentials.add(0, new NameValuePair("", "None"));

        return usernameAndPasswordCredentials;
    }

    private boolean isBlankOrValidGavc(String coordinates) {
        if (StringUtils.isBlank(coordinates)) {
            return true;
        }
        return MavenCoordinates.parseCoordinates(coordinates) != null;
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

    private static boolean isBlankOrValid(Pattern pattern, String value) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return pattern.matcher(value).matches();
    }

    public String save() {
        configurationManager.save(
            new PersistentConfiguration()
                .setServer(server)
                .setAllowUntrustedServer(allowUntrustedServer)
                .setSharedCredentialName(sharedCredentialName)
                .setEnforceUrl(enforceUrl)
                .setPluginRepository(pluginRepository)
                .setPluginRepositoryCredentialName(pluginRepositoryCredentialName)
                .setDevelocityPluginVersion(develocityPluginVersion)
                .setCcudPluginVersion(ccudPluginVersion)
                .setInjectMavenExtension(injectMavenExtension)
                .setInjectCcudExtension(injectCcudExtension)
                .setMavenExtensionCustomCoordinates(mavenExtensionCustomCoordinates)
                .setCcudExtensionCustomCoordinates(ccudExtensionCustomCoordinates)
                .setVcsRepositoryFilter(vcsRepositoryFilter)
                .setGradleCaptureFileFingerprints(gradleCaptureFileFingerprints)
                .setMavenCaptureFileFingerprints(mavenCaptureFileFingerprints)
                .setShortLivedTokenExpiry(shortLivedTokenExpiry)
        );

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

    public boolean isEnforceUrl() {
        return enforceUrl;
    }

    public void setEnforceUrl(boolean enforceUrl) {
        this.enforceUrl = enforceUrl;
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

    public String getPluginRepositoryCredentialName() {
        return pluginRepositoryCredentialName;
    }

    public void setPluginRepositoryCredentialName(String pluginRepositoryCredentialName) {
        this.pluginRepositoryCredentialName = pluginRepositoryCredentialName;
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

    public String getMavenExtensionCustomCoordinates() {
        return mavenExtensionCustomCoordinates;
    }

    public void setMavenExtensionCustomCoordinates(String mavenExtensionCustomCoordinates) {
        this.mavenExtensionCustomCoordinates = mavenExtensionCustomCoordinates;
    }

    public String getCcudExtensionCustomCoordinates() {
        return ccudExtensionCustomCoordinates;
    }

    public void setCcudExtensionCustomCoordinates(String ccudExtensionCustomCoordinates) {
        this.ccudExtensionCustomCoordinates = ccudExtensionCustomCoordinates;
    }

    public String getVcsRepositoryFilter() {
        return vcsRepositoryFilter;
    }

    public void setVcsRepositoryFilter(String vcsRepositoryFilter) {
        this.vcsRepositoryFilter = vcsRepositoryFilter;
    }

    public boolean isGradleCaptureFileFingerprints() {
        return gradleCaptureFileFingerprints;
    }

    public void setGradleCaptureFileFingerprints(boolean gradleCaptureFileFingerprints) {
        this.gradleCaptureFileFingerprints = gradleCaptureFileFingerprints;
    }

    public boolean isMavenCaptureFileFingerprints() {
        return mavenCaptureFileFingerprints;
    }

    public void setMavenCaptureFileFingerprints(boolean mavenCaptureFileFingerprints) {
        this.mavenCaptureFileFingerprints = mavenCaptureFileFingerprints;
    }

    public String getShortLivedTokenExpiry() {
        return shortLivedTokenExpiry;
    }

    public void setShortLivedTokenExpiry(String shortLivedTokenExpiry) {
        this.shortLivedTokenExpiry = shortLivedTokenExpiry;
    }
}
