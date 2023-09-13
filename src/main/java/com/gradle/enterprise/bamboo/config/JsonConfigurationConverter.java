package com.gradle.enterprise.bamboo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonConfigurationConverter {

    private final ObjectMapper objectMapper;

    public JsonConfigurationConverter() {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public @Nullable String toJson(@Nullable Object configuration) throws JsonProcessingException {
        if (configuration == null) {
            return null;
        }
        return objectMapper.writeValueAsString(configuration);
    }

    public @Nullable PersistentConfiguration fromJson(@Nullable String json) throws IOException {
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, PersistentConfiguration.class);
    }
}
