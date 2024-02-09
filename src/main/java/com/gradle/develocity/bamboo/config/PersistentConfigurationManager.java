package com.gradle.develocity.bamboo.config;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class PersistentConfigurationManager {

    static final String CONFIG_V0_KEY = "com.gradle.bamboo.plugins.develocity.config";
    static final String CONFIG_V1_KEY = "com.gradle.bamboo.plugins.develocity.config.v1";
    static final String CURRENT_CONFIG_KEY = CONFIG_V1_KEY;

    private final BandanaManager bandanaManager;

    @Autowired
    public PersistentConfigurationManager(@ComponentImport BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public void save(PersistentConfiguration configuration) {
        try {
            bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, CURRENT_CONFIG_KEY, JsonConfigurationConverter.toJson(configuration));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing configuration as json", e);
        }
    }

    public Optional<PersistentConfiguration> load() {
        try {
            Object value = bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, CURRENT_CONFIG_KEY);
            return Optional.ofNullable(JsonConfigurationConverter.fromJson((String) value));
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing configuration from json", e);
        }
    }
}
