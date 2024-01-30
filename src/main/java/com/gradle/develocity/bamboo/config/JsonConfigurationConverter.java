package com.gradle.develocity.bamboo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

class JsonConfigurationConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonConfigurationConverter() {
    }

    @Nullable
    static String toJson(@Nullable Object configuration) throws JsonProcessingException {
        if (configuration == null) {
            return null;
        }
        return objectMapper.writeValueAsString(configuration);
    }

    @Nullable
    static PersistentConfiguration fromJson(@Nullable String json) throws IOException {
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, PersistentConfiguration.class);
    }
}
