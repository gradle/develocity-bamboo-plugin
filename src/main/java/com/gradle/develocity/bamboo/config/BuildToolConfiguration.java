package com.gradle.develocity.bamboo.config;

import org.jetbrains.annotations.Nullable;

public abstract class BuildToolConfiguration {

    @Nullable
    public final String server;
    public final boolean allowUntrustedServer;
    @Nullable
    public final String sharedCredentialName;

    protected BuildToolConfiguration(@Nullable String server,
                                     boolean allowUntrustedServer,
                                     @Nullable String sharedCredentialName) {
        this.server = server;
        this.allowUntrustedServer = allowUntrustedServer;
        this.sharedCredentialName = sharedCredentialName;
    }

    public abstract boolean isDisabled();

    public final boolean isEnabled() {
        return !isDisabled();
    }
}
