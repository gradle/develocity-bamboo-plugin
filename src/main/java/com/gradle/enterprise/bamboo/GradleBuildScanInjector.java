package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import com.gradle.enterprise.bamboo.config.GradleConfiguration;
import com.gradle.enterprise.bamboo.config.PersistentConfiguration;
import com.gradle.enterprise.bamboo.config.PersistentConfigurationManager;
import com.gradle.enterprise.bamboo.utils.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.gradle.enterprise.bamboo.utils.StringPredicates.endsWith;
import static com.gradle.enterprise.bamboo.utils.StringPredicates.eq;

@Component
public class GradleBuildScanInjector extends AbstractBuildScanInjector<GradleConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleBuildScanInjector.class);

    private static final String HOME = "HOME";

    // Commercial plugin https://bobswift.atlassian.net/wiki/spaces/BGTP/overview
    private static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_KEY = "org.swift.bamboo.groovy:gradle";
    private static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_WRAPPER_KEY = "org.swift.bamboo.groovy:gradlewrapper";
    private static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLEW_KEY = "org.swift.bamboo.groovy:gradlew";

    public static final String SCRIPT_PLUGIN_KEY = "com.atlassian.bamboo.plugins.scripttask:task.builder.script";

    // Artifactory gradle builder: https://github.com/jfrog/bamboo-artifactory-plugin/blob/master/src/main/java/org/jfrog/bamboo/task/ArtifactoryGradleTask.java
    public static final String ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX = "artifactoryGradleTask";

    private static final Set<Predicate<String>> GRADLE_BUILDERS =
        ImmutableSet.of(
            eq(SCRIPT_PLUGIN_KEY),
            eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_KEY),
            eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_WRAPPER_KEY),
            eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLEW_KEY),
            endsWith(ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX)
        );

    private final EnvironmentVariableAccessor environmentVariableAccessor;
    private final GradleEnterpriseAccessKeyExporter accessKeyExporter;
    private final GradleEmbeddedResources gradleEmbeddedResources = new GradleEmbeddedResources();

    @Autowired
    public GradleBuildScanInjector(
        @ComponentImport BuildLoggerManager buildLoggerManager,
        PersistentConfigurationManager configurationManager,
        @ComponentImport EnvironmentVariableAccessor environmentVariableAccessor,
        GradleEnterpriseAccessKeyExporter accessKeyExporter
    ) {
        super(buildLoggerManager, configurationManager);
        this.environmentVariableAccessor = environmentVariableAccessor;
        this.accessKeyExporter = accessKeyExporter;
    }

    @Override
    public boolean isSupported(RuntimeTaskDefinition task) {
        return anyMatch(GRADLE_BUILDERS, task);
    }

    @Override
    public GradleConfiguration buildToolConfiguration(PersistentConfiguration configuration) {
        return GradleConfiguration.of(configuration);
    }

    @Override
    public BuildTool buildTool() {
        return BuildTool.GRADLE;
    }

    @Override
    public void inject(BuildContext buildContext) {
        Map<String, String> environment = environmentVariableAccessor.getEnvironment();
        String home = environment.get(HOME);
        if (StringUtils.isBlank(home)) {
            LOGGER.warn("{} is not set", HOME);
            return;
        }

        Collection<RuntimeTaskDefinition> gradleBuilders = getSupportedTasks(buildContext);
        if (gradleBuilders.isEmpty()) {
            return;
        }

        GradleEmbeddedResources.deleteInitScript(home);
        inject(buildContext, gradleBuilders, home);
    }

    private void inject(BuildContext buildContext, Collection<RuntimeTaskDefinition> tasks, String home) {
        GradleConfiguration config = loadConfiguration().orElse(null);

        if (config == null || config.isDisabled()) {
            LOGGER.debug("Gradle Enterprise Gradle auto-injection is disabled");
            return;
        }

        File initScript = gradleEmbeddedResources.copyInitScript(home);
        LOGGER.debug("Gradle init script: {}", initScript.getAbsolutePath());

        prepareEnvironment(buildContext, config);
        registerGradleEnterpriseResources(buildContext, initScript);
        setupBuildScansLogInterceptor(buildContext);

        accessKeyExporter.exportGradleEnterpriseAccessKey(buildContext, tasks);

        LOGGER.debug("Gradle Enterprise Gradle auto-injection completed");
    }

    private void prepareEnvironment(BuildContext buildContext, GradleConfiguration config) {
        VariableContext variableContext = buildContext.getVariableContext();

        Objects.runIfNotNull(config.server, s -> variableContext.addLocalVariable("GE_PLUGIN_GRADLE_ENTERPRISE_URL", s));
        Objects.runIfTrue(config.allowUntrustedServer, () -> variableContext.addLocalVariable("GE_PLUGIN_GRADLE_ENTERPRISE_ALLOW_UNTRUSTED_SERVER", "true"));
        Objects.runIfNotNull(config.gePluginVersion, v -> variableContext.addLocalVariable("GE_PLUGIN_GRADLE_ENTERPRISE_PLUGIN_VERSION", v));
        Objects.runIfNotNull(config.ccudPluginVersion, v -> variableContext.addLocalVariable("GE_PLUGIN_CCUD_PLUGIN_VERSION", v));
        Objects.runIfNotNull(config.pluginRepository, r -> variableContext.addLocalVariable("GE_PLUGIN_GRADLE_PLUGIN_REPOSITORY_URL", r));
    }
}
