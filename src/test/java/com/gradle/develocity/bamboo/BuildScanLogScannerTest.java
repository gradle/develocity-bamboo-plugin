package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.BuildOutputLogEntry;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BuildScanLogScannerTest {

    private final BuildContext buildContext = TestFixtures.getBuildContext();
    private final BuildScanLogScanner buildScanLogScanner =
        new BuildScanLogScanner(new BuildScanCollector(buildContext));

    @Test
    void buildScanIsNotCollectedAsItsNotPresentInLogs() {
        buildScanLogScanner.intercept(new BuildOutputLogEntry("log without build scan data"));

        assertThat(buildContext.getCurrentResult().getCustomBuildData().containsKey(Constants.BUILD_SCANS_KEY), is(false));
    }

    @Test
    void buildScanIsNotCollectedDueToNonBuildScanUrlType() {
        buildScanLogScanner.intercept(new BuildOutputLogEntry("Publishing build scan..."));
        buildScanLogScanner.intercept(new BuildOutputLogEntry("http://non-buildscan.url/"));

        assertThat(buildContext.getCurrentResult().getCustomBuildData().containsKey(Constants.BUILD_SCANS_KEY), is(false));
    }

    @Test
    void buildScanIsCollected() {
        String buildScanUrl = TestFixtures.randomBuildScanUrl();

        buildScanLogScanner.intercept(new BuildOutputLogEntry("Publishing build scan..."));
        buildScanLogScanner.intercept(new BuildOutputLogEntry(buildScanUrl));

        assertThat(
            buildContext.getCurrentResult().getCustomBuildData().get(Constants.BUILD_SCANS_KEY),
            is(equalTo(buildScanUrl))
        );
    }

    @Test
    void buildScanIsCollectedAndNonBuildScanUrlIsIgnored() {
        String buildScanUrl = TestFixtures.randomBuildScanUrl();

        buildScanLogScanner.intercept(new BuildOutputLogEntry("Publishing build scan..."));
        buildScanLogScanner.intercept(new BuildOutputLogEntry("http://non-buildscan.url/"));
        buildScanLogScanner.intercept(new BuildOutputLogEntry(buildScanUrl));

        assertThat(
            buildContext.getCurrentResult().getCustomBuildData().get(Constants.BUILD_SCANS_KEY),
            is(equalTo(buildScanUrl))
        );
    }

    @Test
    void multipleBuildScansAreCollected() {
        String buildScanUrl = TestFixtures.randomBuildScanUrl();
        String anotherBuildScanUrl = TestFixtures.randomBuildScanUrl();

        Stream.of(
                TestFixtures.randomLogEntries(10),
                Arrays.asList(new BuildOutputLogEntry("Publishing build scan..."), new BuildOutputLogEntry(buildScanUrl)),
                TestFixtures.randomLogEntries(20),
                Arrays.asList(new BuildOutputLogEntry("Publishing build information..."), new BuildOutputLogEntry(anotherBuildScanUrl)),
                TestFixtures.randomLogEntries(30))
            .flatMap(Collection::stream)
            .forEach(buildScanLogScanner::intercept);

        assertThat(
            buildContext.getCurrentResult().getCustomBuildData().get(Constants.BUILD_SCANS_KEY),
            is(equalTo(buildScanUrl + Constants.BUILD_SCANS_SEPARATOR + anotherBuildScanUrl))
        );
    }

}
