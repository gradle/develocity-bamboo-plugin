package com.gradle.develocity.bamboo.config;

import com.atlassian.bamboo.credentials.CredentialsAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsernameAndPasswordCredentialsProvider {

    private final CredentialsAccessor credentialsAccessor;

    @Autowired
    public UsernameAndPasswordCredentialsProvider(@ComponentImport CredentialsAccessor credentialsAccessor) {
        this.credentialsAccessor = credentialsAccessor;
    }

    public Optional<UsernameAndPassword> findByName(String name) {
        return Optional.ofNullable(credentialsAccessor.getCredentialsByName(name))
            .map(UsernameAndPassword::of);
    }
}
