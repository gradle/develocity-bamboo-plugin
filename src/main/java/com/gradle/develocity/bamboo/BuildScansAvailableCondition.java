package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.resultsummary.ResultsSummary;
import com.atlassian.bamboo.resultsummary.ResultsSummaryManager;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.Condition;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Map;

public class BuildScansAvailableCondition implements Condition {

    private ResultsSummaryManager resultsSummaryManager;

    @Override
    public void init(Map<String, String> context) throws PluginParseException {
    }

    @Override
    public boolean shouldDisplay(Map<String, Object> context) {
        String buildKey = ObjectUtils.firstNonNull((String) context.get(Constants.PLAN_KEY_PARAM), (String) context.get(Constants.BUILD_KEY_PARAM));
        String buildNumber = (String) context.get(Constants.BUILD_NUMBER_PARAM);

        if (buildKey == null || buildNumber == null) {
            return false;
        }

        PlanResultKey planResultKey = PlanKeys.getPlanResultKey(buildKey, Integer.parseInt(buildNumber));
        ResultsSummary resultsSummary = resultsSummaryManager.getResultsSummary(planResultKey);
        return ResultsSummaryUtils.hasBuildScans(resultsSummary);
    }

    /**
     * Required for the dependency injection.
     */
    public void setResultsSummaryManager(ResultsSummaryManager resultsSummaryManager) {
        this.resultsSummaryManager = resultsSummaryManager;
    }
}
