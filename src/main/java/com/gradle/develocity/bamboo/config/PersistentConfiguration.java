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

    @Nullable
    private String gePluginVersion;
    @Nullable
    private String ccudPluginVersion;
    @Nullable
    private String pluginRepository;

    private boolean injectMavenExtension;
    private boolean injectCcudExtension;

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

    @Nullable
    public String getGePluginVersion() {
        return gePluginVersion;
    }

    public PersistentConfiguration setGePluginVersion(String gePluginVersion) {
        this.gePluginVersion = StringUtils.trimToNull(gePluginVersion);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("server", server)
            .append("allowUntrustedServer", allowUntrustedServer)
            .append("sharedCredentialName", sharedCredentialName)
            .append("gePluginVersion", gePluginVersion)
            .append("ccudPluginVersion", ccudPluginVersion)
            .append("pluginRepository", pluginRepository)
            .append("injectMavenExtension", injectMavenExtension)
            .append("injectCcudExtension", injectCcudExtension)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistentConfiguration that = (PersistentConfiguration) o;
        return allowUntrustedServer == that.allowUntrustedServer &&
            injectMavenExtension == that.injectMavenExtension &&
            injectCcudExtension == that.injectCcudExtension && Objects.equals(server, that.server) &&
            Objects.equals(sharedCredentialName, that.sharedCredentialName) &&
            Objects.equals(gePluginVersion, that.gePluginVersion) &&
            Objects.equals(ccudPluginVersion, that.ccudPluginVersion) &&
            Objects.equals(pluginRepository, that.pluginRepository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, allowUntrustedServer, sharedCredentialName, gePluginVersion, ccudPluginVersion,
            pluginRepository, injectMavenExtension, injectCcudExtension);
    }
}
