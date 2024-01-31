package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.Ordered;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DevelocityMavenOptsSetter extends Ordered {

    boolean applies(RuntimeTaskDefinition task);

    void apply(RuntimeTaskDefinition task, List<SystemProperty> systemProperties);

    static String mergeMavenOpts(@Nullable String existingMavenOpts, List<SystemProperty> systemProperties) {
        Stream<String> develocityMavenOpts = systemProperties.stream().map(SystemProperty::asString);

        if (StringUtils.isBlank(existingMavenOpts)) {
            return joinOnSpace(develocityMavenOpts);
        }

        Set<String> keys = systemProperties.stream().map(SystemProperty::key).collect(Collectors.toSet());

        Stream<String> filteredExistingMavenOpts = splitOnSpace(existingMavenOpts).filter(v -> keys.stream().noneMatch(v::contains));

        return joinOnSpace(Stream.concat(filteredExistingMavenOpts, develocityMavenOpts));
    }

    static Stream<String> splitOnSpace(String value) {
        return Arrays.stream(value.split(Constants.SPACE));
    }

    static String joinOnSpace(Stream<String> stream) {
        return stream.collect(Collectors.joining(Constants.SPACE));
    }
}
