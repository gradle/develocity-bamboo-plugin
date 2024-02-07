package com.gradle.develocity.bamboo.config;

import org.jetbrains.annotations.Nullable;

public final class GradleConfiguration extends BuildToolConfiguration {

    @Nullable
    public final String develocityPluginVersion;
    @Nullable
    public final String ccudPluginVersion;
    @Nullable
    public final String pluginRepository;

    private GradleConfiguration(@Nullable String server,
                                boolean allowUntrustedServer,
                                @Nullable String sharedCredentialName,
                                @Nullable String develocityPluginVersion,
                                @Nullable String ccudPluginVersion,
                                @Nullable String pluginRepository) {
        super(server, allowUntrustedServer, sharedCredentialName);
        this.develocityPluginVersion = develocityPluginVersion;
        this.ccudPluginVersion = ccudPluginVersion;
        this.pluginRepository = pluginRepository;
    }

    public static GradleConfiguration of(PersistentConfiguration configuration) {
        return new GradleConfiguration(
            configuration.getServer(),
            configuration.isAllowUntrustedServer(),
            configuration.getSharedCredentialName(),
            configuration.getGePluginVersion(),
            configuration.getCcudPluginVersion(),
            configuration.getPluginRepository());
    }

    @Override
    public boolean isDisabled() {
        return server == null || develocityPluginVersion == null;
    }
}
