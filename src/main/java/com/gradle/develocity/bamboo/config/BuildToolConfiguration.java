package com.gradle.develocity.bamboo.config;

import org.jetbrains.annotations.Nullable;

public abstract class BuildToolConfiguration {

    @Nullable
    public final String server;
    public final boolean allowUntrustedServer;
    @Nullable
    public final String sharedCredentialName;
    public final boolean enforceUrl;

    protected BuildToolConfiguration(@Nullable String server,
                                     boolean allowUntrustedServer,
                                     @Nullable String sharedCredentialName,
                                     boolean enforceUrl) {
        this.server = server;
        this.allowUntrustedServer = allowUntrustedServer;
        this.sharedCredentialName = sharedCredentialName;
        this.enforceUrl = enforceUrl;
    }

    public abstract boolean isDisabled();

    public final boolean isEnabled() {
        return !isDisabled();
    }
}
