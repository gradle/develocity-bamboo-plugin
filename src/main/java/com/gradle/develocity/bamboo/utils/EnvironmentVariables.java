package com.gradle.develocity.bamboo.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class EnvironmentVariables {

    private static final char DOUBLE_QUOTATION_MARK = '"';
    private static final char SINGLE_QUOTATION_MARK = '\'';

    private EnvironmentVariables() {
    }

    public static String quoteValueIfNeeded(Map.Entry<String, String> entry) {
        return quoteIfNeeded(entry.getValue());
    }

    public static String quoteIfNeeded(@Nullable String value) {
        if (StringUtils.containsWhitespace(value)) {
            char quotationMark = StringUtils.contains(value, DOUBLE_QUOTATION_MARK)
                ? SINGLE_QUOTATION_MARK
                : DOUBLE_QUOTATION_MARK;

            return String.format("%1$c%2$s%1$c", quotationMark, value);
        }

        return value;
    }
}
