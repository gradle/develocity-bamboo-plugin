package com.gradle.develocity.bamboo.utils;

import java.util.function.Consumer;

public final class Objects {

    private Objects() {
    }

    public static <T> void runIfNotNull(T value, Consumer<T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public static void runIfTrue(boolean shouldRun, Runnable action) {
        if (shouldRun) {
            action.run();
        }
    }

}
