package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.MavenConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.utils.Collections;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.gradle.develocity.bamboo.SystemProperty.SystemPropertyKeyWithDeprecatedKey.*;
import static com.gradle.develocity.bamboo.SystemProperty.SimpleSystemPropertyKey.MAVEN_EXT_CLASS_PATH_SYSTEM_PROPERTY;
import static com.gradle.develocity.bamboo.utils.StringPredicates.endsWith;
import static com.gradle.develocity.bamboo.utils.StringPredicates.eq;

@Component
public class MavenBuildScanInjector extends AbstractBuildScanInjector<MavenConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenBuildScanInjector.class);

    private static final String MAVEN_3_PLUGIN_KEY = "com.atlassian.bamboo.plugins.maven:task.builder.mvn3";

    // Artifactory maven 3 builder: https://github.com/jfrog/bamboo-artifactory-plugin/blob/master/src/main/java/org/jfrog/bamboo/task/ArtifactoryMaven3Task.java
    public static final String ARTIFACTORY_MAVEN_3_TASK_KEY_SUFFIX = "maven3Task";

    private static final Set<Predicate<String>> MAVEN_3_BUILDERS =
        ImmutableSet.of(
            eq(MAVEN_3_PLUGIN_KEY),
            endsWith(ARTIFACTORY_MAVEN_3_TASK_KEY_SUFFIX)
        );

    private static final MavenCoordinates GRADLE_ENTERPRISE_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "gradle-enterprise-maven-extension");
    private static final MavenCoordinates DEVELOCITY_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "develocity-maven-extension");
    private static final MavenCoordinates CCUD_EXTENSION_MAVEN_COORDINATES = new MavenCoordinates("com.gradle", "common-custom-user-data-maven-extension");

    private final DevelocityAccessKeyExporter accessKeyExporter;
    private final List<DevelocityMavenOptsSetter> mavenOptsSetters;
    private final MavenEmbeddedResources mavenEmbeddedResources = new MavenEmbeddedResources();

    @Autowired
    public MavenBuildScanInjector(
        @ComponentImport BuildLoggerManager buildLoggerManager,
        PersistentConfigurationManager configurationManager,
        DevelocityAccessKeyExporter accessKeyExporter,
        List<DevelocityMavenOptsSetter> mavenOptsSetters
    ) {
        super(buildLoggerManager, configurationManager);
        this.accessKeyExporter = accessKeyExporter;
        // ensure consistent order without relying on Spring
        this.mavenOptsSetters = Collections.sortedByOrder(mavenOptsSetters);
    }

    @Override
    public boolean isSupported(RuntimeTaskDefinition task) {
        return anyMatch(MAVEN_3_BUILDERS, task);
    }

    @Override
    public MavenConfiguration buildToolConfiguration(PersistentConfiguration configuration) {
        return MavenConfiguration.of(configuration);
    }

    @Override
    public BuildTool buildTool() {
        return BuildTool.MAVEN;
    }

    @Override
    public void inject(BuildContext buildContext) {
        Collection<RuntimeTaskDefinition> mavenBuilders = getSupportedTasks(buildContext);

        if (mavenBuilders.isEmpty()) {
            return;
        }

        inject(buildContext, mavenBuilders);
    }

    private void inject(BuildContext buildContext, Collection<RuntimeTaskDefinition> tasks) {
        MavenConfiguration config = loadConfiguration().orElse(null);

        if (config == null || config.isDisabled()) {
            LOGGER.debug("Develocity Maven auto-injection is disabled");
            return;
        }

        MavenExtensions existingMavenExtensions = getExistingMavenExtensions(buildContext);

        Classpath classpath = new Classpath();
        List<SystemProperty> systemProperties = new ArrayList<>();
        if (!existingMavenExtensions.hasExtension(GRADLE_ENTERPRISE_EXTENSION_MAVEN_COORDINATES)
                && !existingMavenExtensions.hasExtension(DEVELOCITY_EXTENSION_MAVEN_COORDINATES)
            && !existingMavenExtensions.hasExtension(config.mavenExtensionCustomCoordinates)
        ) {
            classpath.add(mavenEmbeddedResources.copy(MavenEmbeddedResources.Resource.DEVELOCITY_EXTENSION));

            systemProperties.addAll(UPLOAD_IN_BACKGROUND_SYSTEM_PROPERTIES.forValue(false));
            systemProperties.addAll(SERVER_URL_SYSTEM_PROPERTIES.forValue(config.server));
            if (config.allowUntrustedServer) {
                systemProperties.addAll(ALLOW_UNTRUSTED_SERVER_SYSTEM_PROPERTIES.forValue(true));
            }
            systemProperties.addAll(DEVELOCITY_CAPTURE_FILE_FINGERPRINTS_PROPERTY_KEY.forValue(config.mavenCaptureFileFingerprints));
        } else if (!StringUtils.isBlank(config.server) && config.enforceUrl) {
            systemProperties.addAll(SERVER_URL_SYSTEM_PROPERTIES.forValue(config.server));
            if (config.allowUntrustedServer) {
                systemProperties.addAll(ALLOW_UNTRUSTED_SERVER_SYSTEM_PROPERTIES.forValue(true));
            }
        }
        if (config.injectCcudExtension && !existingMavenExtensions.hasExtension(CCUD_EXTENSION_MAVEN_COORDINATES)
            && !existingMavenExtensions.hasExtension(config.ccudExtensionCustomCoordinates)) {
            classpath.add(mavenEmbeddedResources.copy(MavenEmbeddedResources.Resource.CCUD_EXTENSION));
        }

        if (classpath.isNotEmpty()) {
            String mavenExtClasspath = classpath.asString();
            LOGGER.debug("Maven classpath: {}", mavenExtClasspath);

            registerDevelocityResources(buildContext, classpath.files());

            systemProperties.add(MAVEN_EXT_CLASS_PATH_SYSTEM_PROPERTY.forValue(mavenExtClasspath));
        } else {
            LOGGER.debug("Maven classpath is empty due to an existing Develocity and CCUD extension");
        }

        if (!systemProperties.isEmpty()) {
            tasks.forEach(task ->
                mavenOptsSetters
                    .stream()
                    .filter(setter -> setter.applies(task))
                    .findFirst()
                    .ifPresent(setter -> setter.apply(task, systemProperties)));
        }

        setupBuildScansLogInterceptor(buildContext);

        accessKeyExporter.exportDevelocityAccessKey(buildContext, tasks);

        LOGGER.debug("Develocity Maven auto-injection completed");
    }

    private static MavenExtensions getExistingMavenExtensions(BuildContext buildContext) {
        for (String checkoutLocation : buildContext.getCheckoutLocation().values()) {
            File checkoutDirectory = new File(checkoutLocation);
            File extensionsFile = new File(checkoutDirectory, ".mvn/extensions.xml");

            if (extensionsFile.exists()) {
                LOGGER.debug("Found extensions file: {}", extensionsFile);

                return MavenExtensions.fromFile(extensionsFile);
            } else {
                LOGGER.debug("Extensions file not found: {}", extensionsFile);
            }
        }

        return MavenExtensions.empty();
    }
}
