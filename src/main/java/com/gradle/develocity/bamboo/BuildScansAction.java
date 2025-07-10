package com.gradle.develocity.bamboo;

import com.atlassian.annotations.security.AnonymousSiteAccess;
import com.atlassian.bamboo.build.BuildResultsAction;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AnonymousSiteAccess
public class BuildScansAction extends BuildResultsAction {

    private final List<String> buildScans = new ArrayList<>();

    @Override
    public String execute() throws Exception {
        String superResult = super.execute();

        if (ERROR.equals(superResult)) {
            return ERROR;
        }

        ResultsSummary resultsSummary = getBuildResultsSummary();
        if (resultsSummary == null) {
            return ERROR;
        }

        populateBuildScans(resultsSummary);

        return superResult;
    }

    private void populateBuildScans(ResultsSummary resultsSummary) {
        String allBuildScans = resultsSummary.getCustomBuildData().get(Constants.BUILD_SCANS_KEY);
        if (StringUtils.isNotBlank(allBuildScans)) {
            buildScans.addAll(Arrays.asList(allBuildScans.split(Constants.BUILD_SCANS_SEPARATOR)));
        }
    }

    public List<String> getBuildScans() {
        return buildScans;
    }
}
