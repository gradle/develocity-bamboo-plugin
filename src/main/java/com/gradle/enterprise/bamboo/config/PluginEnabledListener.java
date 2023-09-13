package com.gradle.enterprise.bamboo.config;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginEnabledListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginEnabledListener.class);
    private static final String PLUGIN_KEY = "com.gradle.enterprise.gradle-enterprise-bamboo-plugin";
    private final BandanaManager bandanaManager;
    private final JsonConfigurationConverter jsonConfigurationConverter;

    @Autowired
    public PluginEnabledListener(BandanaManager bandanaManager, JsonConfigurationConverter jsonConfigurationConverter) {
        this.bandanaManager = bandanaManager;
        this.jsonConfigurationConverter = jsonConfigurationConverter;
    }

    @EventListener
    public void onPluginEnabled(PluginEnabledEvent event) {
        if (PLUGIN_KEY.equals(event.getPlugin().getKey())) {
            migrateLegacyConfigToV1();
        }
    }

    private void migrateLegacyConfigToV1() {
        Object value = bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, PersistentConfigurationManager.LEGACY_CONFIG_V0_KEY);
        if (value != null && PersistentConfiguration.class.getName().equals(value.getClass().getName())) {
            try {
                LOGGER.info("Migrating {} config to json", PLUGIN_KEY);
                String json = jsonConfigurationConverter.toJson(value);
                bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, PersistentConfigurationManager.CONFIG_V1_KEY, json);
                bandanaManager.removeValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, PersistentConfigurationManager.LEGACY_CONFIG_V0_KEY);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not migrate config to json", e);
            }
        }
    }
}
