package com.gradle.develocity.bamboo.config;

import com.gradle.develocity.bamboo.VcsRepositoryFilter;
import org.jetbrains.annotations.Nullable;

public abstract class BuildToolConfiguration {

    @Nullable
    public final String server;
    public final boolean allowUntrustedServer;
    @Nullable
    public final String sharedCredentialName;
    public final boolean enforceUrl;
    @Nullable
    public VcsRepositoryFilter vcsRepositoryFilter;

    protected BuildToolConfiguration(
            @Nullable String server,
            boolean allowUntrustedServer,
            @Nullable String sharedCredentialName,
            boolean enforceUrl,
            @Nullable String filter
    ) {
        this.server = server;
        this.allowUntrustedServer = allowUntrustedServer;
        this.sharedCredentialName = sharedCredentialName;
        this.enforceUrl = enforceUrl;
        this.vcsRepositoryFilter = VcsRepositoryFilter.of(filter);
    }

    public abstract boolean isDisabled();

    public final boolean isEnabled() {
        return !isDisabled();
    }
}
