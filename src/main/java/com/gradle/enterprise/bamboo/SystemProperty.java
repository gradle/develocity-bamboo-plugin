package com.gradle.enterprise.bamboo;

final class SystemProperty {

    private final String key;
    private final String value;

    SystemProperty(String key, String value) {
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
