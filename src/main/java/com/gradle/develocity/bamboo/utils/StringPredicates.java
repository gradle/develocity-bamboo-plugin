package com.gradle.develocity.bamboo.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

public final class StringPredicates {

    private StringPredicates() {
    }

    public static Predicate<String> eq(String expected) {
        return new Equals(expected);
    }

    public static Predicate<String> endsWith(String suffix) {
        return new EndsWith(suffix);
    }

    private static final class Equals implements Predicate<String> {

        private final String expected;

        private Equals(String expected) {
            this.expected = expected;
        }

        @Override
        public boolean test(String value) {
            return StringUtils.equals(value, expected);
        }
    }

    private static final class EndsWith implements Predicate<String> {

        private final String suffix;

        private EndsWith(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public boolean test(String value) {
            return StringUtils.endsWith(value, suffix);
        }
    }
}
