package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.resultsummary.ResultsSummary;

public final class ResultsSummaryUtils {

    private ResultsSummaryUtils() {
    }

    public static boolean hasBuildScans(ResultsSummary resultsSummary) {
        return resultsSummary != null && resultsSummary.getCustomBuildData().containsKey(Constants.BUILD_SCANS_KEY);
    }
}
