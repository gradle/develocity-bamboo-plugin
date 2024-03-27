package com.gradle.develocity.bamboo.config;

import com.gradle.develocity.bamboo.MavenCoordinates;
import org.jetbrains.annotations.Nullable;

public final class MavenConfiguration extends BuildToolConfiguration {

    public boolean injectMavenExtension;
    public boolean injectCcudExtension;

    public MavenCoordinates mavenExtensionCustomCoordinates;
    public MavenCoordinates ccudExtensionCustomCoordinates;

    private MavenConfiguration(@Nullable String server,
                               boolean allowUntrustedServer,
                               @Nullable String sharedCredentialName,
                               boolean injectMavenExtension,
                               boolean injectCcudExtension,
                               @Nullable String mavenExtensionCustomCoordinates,
                               @Nullable String ccudExtensionCustomCoordinates
                               ) {
        super(server, allowUntrustedServer, sharedCredentialName);
        this.injectMavenExtension = injectMavenExtension;
        this.injectCcudExtension = injectCcudExtension;
        this.mavenExtensionCustomCoordinates = MavenCoordinates.parseCoordinates(mavenExtensionCustomCoordinates);
        this.ccudExtensionCustomCoordinates = MavenCoordinates.parseCoordinates(ccudExtensionCustomCoordinates);
    }

    public static MavenConfiguration of(PersistentConfiguration configuration) {
        return new MavenConfiguration(
            configuration.getServer(),
            configuration.isAllowUntrustedServer(),
            configuration.getSharedCredentialName(),
            configuration.isInjectMavenExtension(),
            configuration.isInjectCcudExtension(),
            configuration.getMavenExtensionCustomCoordinates(),
            configuration.getCcudExtensionCustomCoordinates()
            );
    }

    @Override
    public boolean isDisabled() {
        return server == null || !injectMavenExtension;
    }
}
