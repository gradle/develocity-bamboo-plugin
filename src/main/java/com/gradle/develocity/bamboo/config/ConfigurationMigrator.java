package com.gradle.develocity.bamboo.config;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.gradle.develocity.bamboo.config.PersistentConfigurationManager.CONFIG_V0_KEY;
import static com.gradle.develocity.bamboo.config.PersistentConfigurationManager.CONFIG_V1_KEY;

@Component
public class ConfigurationMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationMigrator.class);
    private static final String DEVELOCITY_BAMBOO_PLUGIN_KEY = "com.gradle.develocity.develocity-bamboo-plugin";
    private final BandanaManager bandanaManager;

    @Autowired
    public ConfigurationMigrator(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        if (DEVELOCITY_BAMBOO_PLUGIN_KEY.equals(event.getPlugin().getKey())) {
            migrateConfigV0ToV1();
        }
    }

    private void migrateConfigV0ToV1() {
        Object value = bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, CONFIG_V0_KEY);
        if (value != null && PersistentConfiguration.class.getName().equals(value.getClass().getName())) {
            try {
                LOGGER.info("Migrating {} config from {} to {}", DEVELOCITY_BAMBOO_PLUGIN_KEY,
                    CONFIG_V0_KEY, CONFIG_V1_KEY);
                String json = JsonConfigurationConverter.toJson(value);
                bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, CONFIG_V1_KEY, json);
                bandanaManager.removeValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, CONFIG_V0_KEY);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not migrate config to json", e);
            }
        }
    }
}
