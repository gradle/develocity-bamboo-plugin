package com.gradle.develocity.bamboo.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PersistentConfiguration {

    @Nullable
    private String server;
    private boolean allowUntrustedServer;
    @Nullable
    private String sharedCredentialName;
    private boolean enforceUrl;

    @Nullable
    private String develocityPluginVersion;
    @Nullable
    private String ccudPluginVersion;
    @Nullable
    private String pluginRepository;
    @Nullable
    private String pluginRepositoryCredentialName;

    private boolean injectMavenExtension;
    private boolean injectCcudExtension;

    @Nullable
    private String mavenExtensionCustomCoordinates;

    @Nullable
    private String ccudExtensionCustomCoordinates;

    @Nullable
    public String getServer() {
        return server;
    }

    public PersistentConfiguration setServer(String server) {
        this.server = StringUtils.trimToNull(server);
        return this;
    }

    public boolean isAllowUntrustedServer() {
        return allowUntrustedServer;
    }

    public PersistentConfiguration setAllowUntrustedServer(boolean allowUntrustedServer) {
        this.allowUntrustedServer = allowUntrustedServer;
        return this;
    }

    @Nullable
    public String getSharedCredentialName() {
        return sharedCredentialName;
    }

    public PersistentConfiguration setSharedCredentialName(@Nullable String sharedCredentialName) {
        this.sharedCredentialName = StringUtils.trimToNull(sharedCredentialName);
        return this;
    }

    public boolean isEnforceUrl() {
        return enforceUrl;
    }

    public PersistentConfiguration setEnforceUrl(boolean enforceUrl) {
        this.enforceUrl = enforceUrl;
        return this;
    }

    @Nullable
    public String getDevelocityPluginVersion() {
        return develocityPluginVersion;
    }

    public PersistentConfiguration setDevelocityPluginVersion(String develocityPluginVersion) {
        this.develocityPluginVersion = StringUtils.trimToNull(develocityPluginVersion);
        return this;
    }

    @Nullable
    public String getCcudPluginVersion() {
        return ccudPluginVersion;
    }

    public PersistentConfiguration setCcudPluginVersion(String ccudPluginVersion) {
        this.ccudPluginVersion = StringUtils.trimToNull(ccudPluginVersion);
        return this;
    }

    @Nullable
    public String getPluginRepository() {
        return pluginRepository;
    }

    public PersistentConfiguration setPluginRepository(String pluginRepository) {
        this.pluginRepository = StringUtils.trimToNull(pluginRepository);
        return this;
    }

    @Nullable
    public String getPluginRepositoryCredentialName() {
        return pluginRepositoryCredentialName;
    }

    public PersistentConfiguration setPluginRepositoryCredentialName(@Nullable String pluginRepositoryCredentialName) {
        this.pluginRepositoryCredentialName = StringUtils.trimToNull(pluginRepositoryCredentialName);
        return this;
    }

    public boolean isInjectMavenExtension() {
        return injectMavenExtension;
    }

    public PersistentConfiguration setInjectMavenExtension(boolean injectMavenExtension) {
        this.injectMavenExtension = injectMavenExtension;
        return this;
    }

    public boolean isInjectCcudExtension() {
        return injectCcudExtension;
    }

    public PersistentConfiguration setInjectCcudExtension(boolean injectCcudExtension) {
        this.injectCcudExtension = injectCcudExtension;
        return this;
    }

    @Nullable
    public String getMavenExtensionCustomCoordinates() {
        return mavenExtensionCustomCoordinates;
    }

    public PersistentConfiguration setMavenExtensionCustomCoordinates(@Nullable String mavenExtensionCustomCoordinates) {
        this.mavenExtensionCustomCoordinates = mavenExtensionCustomCoordinates;
        return this;
    }

    @Nullable
    public String getCcudExtensionCustomCoordinates() {
        return ccudExtensionCustomCoordinates;
    }

    public PersistentConfiguration setCcudExtensionCustomCoordinates(@Nullable String ccudExtensionCustomCoordinates) {
        this.ccudExtensionCustomCoordinates = ccudExtensionCustomCoordinates;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("server", server)
            .append("allowUntrustedServer", allowUntrustedServer)
            .append("enforceUrl", enforceUrl)
            .append("sharedCredentialName", sharedCredentialName)
            .append("develocityPluginVersion", develocityPluginVersion)
            .append("ccudPluginVersion", ccudPluginVersion)
            .append("pluginRepository", pluginRepository)
            .append("pluginRepositoryCredentialName", pluginRepositoryCredentialName)
            .append("injectMavenExtension", injectMavenExtension)
            .append("injectCcudExtension", injectCcudExtension)
            .append("customMavenExtension", mavenExtensionCustomCoordinates)
            .append("customCcudExtension", ccudExtensionCustomCoordinates)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistentConfiguration that = (PersistentConfiguration) o;
        return allowUntrustedServer == that.allowUntrustedServer &&
            enforceUrl == that.enforceUrl &&
            injectMavenExtension == that.injectMavenExtension &&
            injectCcudExtension == that.injectCcudExtension &&
            Objects.equals(server, that.server) &&
            Objects.equals(sharedCredentialName, that.sharedCredentialName) &&
            Objects.equals(develocityPluginVersion, that.develocityPluginVersion) &&
            Objects.equals(ccudPluginVersion, that.ccudPluginVersion) &&
            Objects.equals(pluginRepository, that.pluginRepository) &&
            Objects.equals(pluginRepositoryCredentialName, that.pluginRepositoryCredentialName) &&
            Objects.equals(mavenExtensionCustomCoordinates, that.mavenExtensionCustomCoordinates) &&
            Objects.equals(ccudExtensionCustomCoordinates, that.ccudExtensionCustomCoordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, allowUntrustedServer, sharedCredentialName, enforceUrl, develocityPluginVersion, ccudPluginVersion,
            pluginRepository, pluginRepositoryCredentialName, injectMavenExtension, injectCcudExtension, mavenExtensionCustomCoordinates, ccudExtensionCustomCoordinates);
    }
}
