package com.gradle.enterprise.bamboo;

public final class Constants {

    public static final String PLAN_KEY_PARAM = "planKey";
    public static final String BUILD_KEY_PARAM = "buildKey";
    public static final String BUILD_NUMBER_PARAM = "buildNumber";

    public static final String BUILD_SCANS_KEY = "buildScans";
    public static final String BUILD_SCANS_SEPARATOR = ",";

    public static final String GE_RESOURCES_KEY = "gradleEnterprise.resources";
    public static final String GE_RESOURCES_SEPARATOR = ",";

    // must have secret as part of the name, see com.atlassian.bamboo.util.PasswordMaskingUtils
    public static final String ACCESS_KEY = "gradleEnterprise.secret.accessKey";

    public static final String GRADLE_ENTERPRISE_ACCESS_KEY = "GRADLE_ENTERPRISE_ACCESS_KEY";

    public static final String SPACE = " ";

    private Constants() {
    }
}
