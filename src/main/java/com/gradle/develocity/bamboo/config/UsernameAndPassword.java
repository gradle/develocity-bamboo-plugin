package com.gradle.develocity.bamboo.config;

import com.atlassian.bamboo.credentials.CredentialsData;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class UsernameAndPassword {

    public static final String SHARED_USERNAME_PASSWORD_PLUGIN_KEY = "com.atlassian.bamboo.plugin.sharedCredentials:usernamePasswordCredentials";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

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
