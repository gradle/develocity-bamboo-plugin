package com.gradle.develocity.bamboo.config;

import org.jetbrains.annotations.Nullable;

public final class MavenConfiguration extends BuildToolConfiguration {

    public boolean injectMavenExtension;
    public boolean injectCcudExtension;

    private MavenConfiguration(@Nullable String server,
                               boolean allowUntrustedServer,
                               @Nullable String sharedCredentialName,
                               boolean injectMavenExtension,
                               boolean injectCcudExtension) {
        super(server, allowUntrustedServer, sharedCredentialName);
        this.injectMavenExtension = injectMavenExtension;
        this.injectCcudExtension = injectCcudExtension;
    }

    public static MavenConfiguration of(PersistentConfiguration configuration) {
        return new MavenConfiguration(
            configuration.getServer(),
            configuration.isAllowUntrustedServer(),
            configuration.getSharedCredentialName(),
            configuration.isInjectMavenExtension(),
            configuration.isInjectCcudExtension());
    }

    @Override
    public boolean isDisabled() {
        return server == null || !injectMavenExtension;
    }
}
