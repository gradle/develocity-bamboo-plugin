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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

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

    private final GradleEnterpriseAccessKeyExporter accessKeyExporter;
    private final List<GradleEnterpriseMavenOptsSetter> mavenOptsSetters;
    private final MavenEmbeddedResources mavenEmbeddedResources = new MavenEmbeddedResources();

    @Autowired
    public MavenBuildScanInjector(
        @ComponentImport BuildLoggerManager buildLoggerManager,
        PersistentConfigurationManager configurationManager,
        GradleEnterpriseAccessKeyExporter accessKeyExporter,
        List<GradleEnterpriseMavenOptsSetter> mavenOptsSetters
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

        Classpath classpath = new Classpath();
        classpath.add(mavenEmbeddedResources.copy(MavenEmbeddedResources.Resource.DEVELOCITY_EXTENSION));
        if (config.injectCcudExtension) {
            classpath.add(mavenEmbeddedResources.copy(MavenEmbeddedResources.Resource.CCUD_EXTENSION));
        }

        String mavenExtClasspath = classpath.asString();
        LOGGER.debug("Maven classpath: {}", mavenExtClasspath);

        registerGradleEnterpriseResources(buildContext, classpath.files());

        List<SystemProperty> systemProperties = new ArrayList<>();
        systemProperties.add(new SystemProperty("maven.ext.class.path", mavenExtClasspath));
        systemProperties.add(new SystemProperty("gradle.scan.uploadInBackground", "false"));
        systemProperties.add(new SystemProperty("gradle.enterprise.url", config.server));
        if (config.allowUntrustedServer) {
            systemProperties.add(new SystemProperty("gradle.enterprise.allowUntrustedServer", "true"));
        }

        tasks.forEach(task ->
            mavenOptsSetters
                .stream()
                .filter(setter -> setter.applies(task))
                .findFirst()
                .ifPresent(setter -> setter.apply(task, systemProperties)));

        setupBuildScansLogInterceptor(buildContext);

        accessKeyExporter.exportGradleEnterpriseAccessKey(buildContext, tasks);

        LOGGER.debug("Develocity Maven auto-injection completed");
    }
}
