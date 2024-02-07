package com.gradle.develocity.bamboo.model;

public final class TriggeredBuild {

    private String planKey;
    private Integer buildNumber;
    private String buildResultKey;
    private String triggerReason;

    public String getPlanKey() {
        return planKey;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public String getBuildResultKey() {
        return buildResultKey;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    @Override
    public String toString() {
        return "TriggeredBuild{" +
                "planKey='" + planKey + '\'' +
                ", buildNumber=" + buildNumber +
                ", buildResultKey='" + buildResultKey + '\'' +
                ", triggerReason='" + triggerReason + '\'' +
                '}';
    }

}
