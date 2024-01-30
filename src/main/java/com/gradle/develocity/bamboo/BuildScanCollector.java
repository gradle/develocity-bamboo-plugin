package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

final class BuildScanCollector implements Consumer<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildScanCollector.class);

    private final BuildContext buildContext;

    BuildScanCollector(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    @Override
    public void accept(String buildScan) {
        CurrentResult currentResult = buildContext.getCurrentResult();

        if (currentResult != null) {
            LOGGER.debug("A build scan has been found: {}", buildScan);

            currentResult
                .getCustomBuildData()
                .merge(Constants.BUILD_SCANS_KEY, buildScan, BuildScanCollector::concat);
        }
    }

    private static String concat(String existingBuildScans, String newBuildScan) {
        return String.join(Constants.BUILD_SCANS_SEPARATOR, existingBuildScans, newBuildScan);
    }
}
