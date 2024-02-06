package com.gradle.develocity.bamboo;

public final class Constants {

    public static final String PLAN_KEY_PARAM = "planKey";
    public static final String BUILD_KEY_PARAM = "buildKey";
    public static final String BUILD_NUMBER_PARAM = "buildNumber";

    public static final String BUILD_SCANS_KEY = "buildScans";
    public static final String BUILD_SCANS_SEPARATOR = ",";

    public static final String DEVELOCITY_RESOURCES_KEY = "develocity.resources";
    public static final String DEVELOCITY_RESOURCES_SEPARATOR = ",";

    // must have secret as part of the name, see com.atlassian.bamboo.util.PasswordMaskingUtils
    public static final String ACCESS_KEY = "develocity.secret.accessKey";

    public static final String DEVELOCITY_ACCESS_KEY = "GRADLE_ENTERPRISE_ACCESS_KEY";

    public static final String SPACE = " ";

    public static final String DEFAULT_TASK_ENVIRONMENT_VARIABLES_KEY = "environmentVariables";

    private Constants() {
    }
}
