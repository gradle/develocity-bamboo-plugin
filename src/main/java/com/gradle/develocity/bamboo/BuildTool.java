package com.gradle.develocity.bamboo;

public enum BuildTool {

    GRADLE("Gradle"),
    MAVEN("Maven");

    private final String displayName;

    BuildTool(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

}
