package com.gradle.enterprise.bamboo.config;

import com.atlassian.bamboo.credentials.CredentialsData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class UsernameAndPassword {

    private static final String SHARED_USERNAME_PASSWORD_PLUGIN_KEY = "com.atlassian.bamboo.plugin.sharedCredentials:usernamePasswordCredentials";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private final CredentialsData data;

    private UsernameAndPassword(CredentialsData data) {
        this.data = data;
    }

    @Nullable
    public static UsernameAndPassword of(CredentialsData data) {
        if (!Objects.equals(SHARED_USERNAME_PASSWORD_PLUGIN_KEY, data.getPluginKey())) {
            return null;
        }
        return new UsernameAndPassword(data);
    }

    @Nullable
    public String getUsername() {
        return data.getConfiguration().get(USERNAME);
    }

    @Nullable
    public String getPassword() {
        return data.getConfiguration().get(PASSWORD);
    }
}
