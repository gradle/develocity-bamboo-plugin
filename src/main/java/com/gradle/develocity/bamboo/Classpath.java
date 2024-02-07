package com.gradle.develocity.bamboo;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class Classpath {

    private final List<File> files = new ArrayList<>();

    void add(File file) {
        files.add(file);
    }

    File[] files() {
        return files.toArray(new File[0]);
    }

    String asString() {
        return files.stream()
            .map(File::getAbsolutePath)
            .collect(Collectors.joining(classpathSeparator()));
    }

    private static String classpathSeparator() {
        return SystemUtils.IS_OS_WINDOWS ? ";" : ":";
    }

    boolean isNotEmpty() {
        return !files.isEmpty();
    }

}
