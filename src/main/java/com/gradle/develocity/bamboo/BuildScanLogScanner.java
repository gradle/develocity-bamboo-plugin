package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.LogEntry;
import com.atlassian.bamboo.build.logger.LogInterceptorAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BuildScanLogScanner extends LogInterceptorAdapter {

    private static final Pattern BUILD_SCAN_PATTERN = Pattern.compile("Publishing (build scan|build information)\\.\\.\\.");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S*/s/\\S*");

    private static final int LOOK_AHEAD_LINES = 10;

    private int linesSinceBuildScanPublishingMessage = Integer.MAX_VALUE;

    private final Consumer<String> buildScanConsumer;

    BuildScanLogScanner(Consumer<String> buildScanConsumer) {
        this.buildScanConsumer = buildScanConsumer;
    }

    @Override
    public void intercept(@NotNull LogEntry logEntry) {
        String line = logEntry.getLog();

        if (linesSinceBuildScanPublishingMessage < LOOK_AHEAD_LINES) {
            linesSinceBuildScanPublishingMessage++;
            Matcher matcher = URL_PATTERN.matcher(line);
            if (matcher.find()) {
                linesSinceBuildScanPublishingMessage = Integer.MAX_VALUE;
                String buildScanUrl = matcher.group();
                buildScanConsumer.accept(buildScanUrl);
            }
        }

        if (BUILD_SCAN_PATTERN.matcher(line).find()) {
            linesSinceBuildScanPublishingMessage = 0;
        }
    }
}
