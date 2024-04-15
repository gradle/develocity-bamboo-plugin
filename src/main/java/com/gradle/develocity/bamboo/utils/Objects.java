package com.gradle.develocity.bamboo.utils;

import com.google.common.base.Throwables;

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

    public static Runnable run(ThrowingRunnable<? extends Exception> throwing) {
        return throwing.asRunnable();
    }

    @FunctionalInterface
    public interface ThrowingRunnable<T extends Exception> {
        void run() throws T;

        default Runnable asRunnable() {
            return () -> {
                try {
                    this.run();
                } catch(Exception e) {
                    Throwables.throwIfUnchecked(e);
                    throw new RuntimeException(e);
                }
            };
        }
    }

}
