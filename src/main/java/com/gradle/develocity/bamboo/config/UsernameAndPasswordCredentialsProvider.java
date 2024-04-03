package com.gradle.develocity.bamboo.config;

import com.atlassian.bamboo.credentials.CredentialsAccessor;
import com.atlassian.bamboo.credentials.CredentialsData;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.gradle.develocity.bamboo.config.UsernameAndPassword.SHARED_USERNAME_PASSWORD_PLUGIN_KEY;

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

    public List<String> getAllUsernameAndPasswordCredentials() {
        return StreamSupport.stream(credentialsAccessor.getAllCredentials().spliterator(), false)
                .filter(it -> SHARED_USERNAME_PASSWORD_PLUGIN_KEY.equals(it.getPluginKey()))
                .map(CredentialsData::getName)
                .collect(Collectors.toList());
    }

}
