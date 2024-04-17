package com.gradle.develocity.bamboo;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class DevelocityAccessCredential {

    private final String hostname;
    private final String key;

    private DevelocityAccessCredential(String hostname, String key) {
        this.hostname = hostname;
        this.key = key;
    }

    public static DevelocityAccessCredential of(String hostname, String key) {
        return new DevelocityAccessCredential(hostname, key);
    }

    public static Optional<DevelocityAccessCredential> parse(String rawAccessKey, String host) {
        return Arrays.stream(rawAccessKey.split(";"))
                .map(k -> k.split("="))
                .filter(hostKey -> hostKey[0].equals(host))
                .map(hostKey -> new DevelocityAccessCredential(hostKey[0], hostKey[1]))
                .findFirst();
    }

    public static boolean isValid(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        String[] entries = value.split(";");

        for (String entry : entries) {
            String[] parts = entry.split("=", 2);
            if (parts.length < 2) {
                return false;
            }

            String servers = parts[0];
            String accessKey = parts[1];

            if (StringUtils.isBlank(servers) || StringUtils.isBlank(accessKey)) {
                return false;
            }

            for (String server : servers.split(",")) {
                if (StringUtils.isBlank(server)) {
                    return false;
                }
            }
        }

        return true;
    }

    public String getRawAccessKey() {
        return hostname + "=" + key;
    }

    public String getHostname() {
        return hostname;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DevelocityAccessCredential that = (DevelocityAccessCredential) o;
        return Objects.equals(hostname, that.hostname) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostname, key);
    }

}
