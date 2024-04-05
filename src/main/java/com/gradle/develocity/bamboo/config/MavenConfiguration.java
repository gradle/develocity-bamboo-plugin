package com.gradle.develocity.bamboo.config;

import com.gradle.develocity.bamboo.MavenCoordinates;
import org.jetbrains.annotations.Nullable;

public final class MavenConfiguration extends BuildToolConfiguration {

    public final boolean injectMavenExtension;
    public final boolean injectCcudExtension;
    public final MavenCoordinates mavenExtensionCustomCoordinates;
    public final MavenCoordinates ccudExtensionCustomCoordinates;
    public final boolean mavenCaptureFileFingerprints;

    private MavenConfiguration(
            @Nullable String server,
            boolean allowUntrustedServer,
            @Nullable String sharedCredentialName,
            boolean injectMavenExtension,
            boolean injectCcudExtension,
            @Nullable String mavenExtensionCustomCoordinates,
            @Nullable String ccudExtensionCustomCoordinates,
            boolean enforceUrl,
            String filter,
            boolean mavenCaptureFileFingerprints
    ) {
        super(server, allowUntrustedServer, sharedCredentialName, enforceUrl, filter);
        this.injectMavenExtension = injectMavenExtension;
        this.injectCcudExtension = injectCcudExtension;
        this.mavenExtensionCustomCoordinates = MavenCoordinates.parseCoordinates(mavenExtensionCustomCoordinates);
        this.ccudExtensionCustomCoordinates = MavenCoordinates.parseCoordinates(ccudExtensionCustomCoordinates);
        this.mavenCaptureFileFingerprints = mavenCaptureFileFingerprints;
    }

    public static MavenConfiguration of(PersistentConfiguration configuration) {
        return new MavenConfiguration(
            configuration.getServer(),
            configuration.isAllowUntrustedServer(),
            configuration.getSharedCredentialName(),
            configuration.isInjectMavenExtension(),
            configuration.isInjectCcudExtension(),
            configuration.getMavenExtensionCustomCoordinates(),
            configuration.getCcudExtensionCustomCoordinates(),
            configuration.isEnforceUrl(),
            configuration.getVcsRepositoryFilter(),
            configuration.isMavenCaptureFileFingerprints()
        );
    }

    @Override
    public boolean isDisabled() {
        return server == null || !injectMavenExtension;
    }
}
