package com.gradle.enterprise.bamboo.config;

import com.atlassian.bamboo.credentials.CredentialsAccessor;
import com.atlassian.bamboo.credentials.CredentialsData;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsernameAndPasswordCredentialsProvider {

    private static final String SHARED_USERNAME_PASSWORD_PLUGIN_KEY = "com.atlassian.bamboo.plugin.sharedCredentials:usernamePasswordCredentials";

    private final CredentialsAccessor credentialsAccessor;

    @Autowired
    public UsernameAndPasswordCredentialsProvider(@ComponentImport CredentialsAccessor credentialsAccessor) {
        this.credentialsAccessor = credentialsAccessor;
    }

    public boolean exists(String name) {
        return findByName(name).isPresent();
    }

    public Optional<CredentialsData> findByName(String name) {
        return Optional.ofNullable(credentialsAccessor.getCredentialsByName(name))
            .filter(d -> SHARED_USERNAME_PASSWORD_PLUGIN_KEY.equals(d.getPluginKey()));
    }
}
