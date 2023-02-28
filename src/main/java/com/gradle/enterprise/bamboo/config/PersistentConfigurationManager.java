package com.gradle.enterprise.bamboo.config;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PersistentConfigurationManager {

    private static final String KEY = "com.gradle.bamboo.plugins.ge.config";

    private final BandanaManager bandanaManager;

    @Autowired
    public PersistentConfigurationManager(@ComponentImport BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public void save(PersistentConfiguration configuration) {
        bandanaManager.setValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, KEY, configuration);
    }

    public Optional<PersistentConfiguration> load() {
        return Optional.ofNullable(
            (PersistentConfiguration) bandanaManager.getValue(PlanAwareBandanaContext.GLOBAL_CONTEXT, KEY)
        );
    }
}
