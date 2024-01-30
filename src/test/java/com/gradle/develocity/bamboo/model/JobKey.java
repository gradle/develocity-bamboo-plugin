package com.gradle.develocity.bamboo.model;

public final class JobKey {

    private String key;

    public String getValue() {
        return key;
    }

    @Override
    public String toString() {
        return "JobKey{" +
                "key='" + key + '\'' +
                '}';
    }
}
