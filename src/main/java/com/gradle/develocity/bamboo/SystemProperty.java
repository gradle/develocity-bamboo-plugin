package com.gradle.develocity.bamboo;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public final class SystemProperty {

    enum SimpleSystemPropertyKey {
        MAVEN_EXT_CLASS_PATH_SYSTEM_PROPERTY("maven.ext.class.path");

        SimpleSystemPropertyKey(String key) {
            this.key = key;
        }

        public SystemProperty forValue(String value) {
            return new SystemProperty(key, value);
        }

        private final String key;
    }

    enum SystemPropertyKeyWithDeprecatedKey {

        UPLOAD_IN_BACKGROUND_SYSTEM_PROPERTIES("develocity.scan.uploadInBackground", "gradle.scan.uploadInBackground"),
        SERVER_URL_SYSTEM_PROPERTIES("develocity.url", "gradle.enterprise.url"),
        ALLOW_UNTRUSTED_SERVER_SYSTEM_PROPERTIES("develocity.allowUntrustedServer", "gradle.enterprise.allowUntrustedServer"),
        DEVELOCITY_CAPTURE_FILE_FINGERPRINTS_PROPERTY_KEY("develocity.scan.captureFileFingerprints", "gradle.scan.captureGoalInputFiles");

        private final String develocityKey;

        private final String deprecatedKey;

        SystemPropertyKeyWithDeprecatedKey(String develocityKey, String deprecatedKey) {
            this.develocityKey = develocityKey;
            this.deprecatedKey = deprecatedKey;
        }

        public Collection<SystemProperty> forValue(String value) {
            return propertiesFor(value);
        }

        public Collection<SystemProperty> forValue(boolean value) {
            return propertiesFor(Boolean.toString(value));
        }

        private Collection<SystemProperty> propertiesFor(@Nullable String value) {
            Collection<SystemProperty> properties = new ArrayList<>();

            String val = (value == null) ? "" : value;

            properties.add(new SystemProperty(develocityKey, val));
            properties.add(new SystemProperty(deprecatedKey, val));

            return properties;
        }

    }

    private final String key;
    private final String value;

    private SystemProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    String key() {
        return key;
    }

    String asString() {
        return String.format("-D%s=%s", key, value);
    }

}
