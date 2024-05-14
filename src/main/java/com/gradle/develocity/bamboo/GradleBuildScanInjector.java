package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.process.EnvironmentVariableAccessor;
import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableContext;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableSet;
import com.gradle.develocity.bamboo.config.GradleConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import com.gradle.develocity.bamboo.config.UsernameAndPassword;
import com.gradle.develocity.bamboo.config.UsernameAndPasswordCredentialsProvider;
import com.gradle.develocity.bamboo.utils.Objects;
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

import static com.gradle.develocity.bamboo.utils.StringPredicates.endsWith;
import static com.gradle.develocity.bamboo.utils.StringPredicates.eq;

@Component
public class GradleBuildScanInjector extends AbstractBuildScanInjector<GradleConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleBuildScanInjector.class);

    private static final String HOME = "HOME";

    // Commercial plugin https://bobswift.atlassian.net/wiki/spaces/BGTP/overview
    public static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_KEY = "org.swift.bamboo.groovy:gradle";
    public static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_WRAPPER_KEY = "org.swift.bamboo.groovy:gradlewrapper";
    public static final String BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLEW_KEY = "org.swift.bamboo.groovy:gradlew";

    public static final String SCRIPT_PLUGIN_KEY = "com.atlassian.bamboo.plugins.scripttask:task.builder.script";
    public static final String COMMAND_PLUGIN_KEY = "com.atlassian.bamboo.plugins.scripttask:task.builder.command";

    // Artifactory gradle builder: https://github.com/jfrog/bamboo-artifactory-plugin/blob/master/src/main/java/org/jfrog/bamboo/task/ArtifactoryGradleTask.java
    public static final String ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX = "artifactoryGradleTask";

    private static final Set<Predicate<String>> GRADLE_BUILDERS =
            ImmutableSet.of(
                    eq(SCRIPT_PLUGIN_KEY),
                    eq(COMMAND_PLUGIN_KEY),
                    eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_KEY),
                    eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLE_WRAPPER_KEY),
                    eq(BOB_SWIFT_GROOVY_TASKS_PLUGIN_GRADLEW_KEY),
                    endsWith(ARTIFACTORY_GRADLE_TASK_KEY_SUFFIX)
            );

    private final EnvironmentVariableAccessor environmentVariableAccessor;
    private final DevelocityAccessKeyExporter accessKeyExporter;
    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final GradleEmbeddedResources gradleEmbeddedResources = new GradleEmbeddedResources();

    @Autowired
    public GradleBuildScanInjector(
            @ComponentImport BuildLoggerManager buildLoggerManager,
            PersistentConfigurationManager configurationManager,
            @ComponentImport EnvironmentVariableAccessor environmentVariableAccessor,
            DevelocityAccessKeyExporter accessKeyExporter,
            UsernameAndPasswordCredentialsProvider credentialsProvider
    ) {
        super(buildLoggerManager, configurationManager);
        this.environmentVariableAccessor = environmentVariableAccessor;
        this.accessKeyExporter = accessKeyExporter;
        this.credentialsProvider = credentialsProvider;
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
            LOGGER.debug("Develocity Gradle auto-injection is disabled");
            return;
        }

        File initScript = gradleEmbeddedResources.copyInitScript(home);
        LOGGER.debug("Gradle init script: {}", initScript.getAbsolutePath());

        prepareEnvironment(buildContext, config);
        registerDevelocityResources(buildContext, initScript);
        setupBuildScansLogInterceptor(buildContext);

        accessKeyExporter.exportDevelocityAccessKey(buildContext, tasks);

        LOGGER.debug("Develocity Gradle auto-injection completed");
    }

    private void prepareEnvironment(BuildContext buildContext, GradleConfiguration config) {
        VariableContext variableContext = buildContext.getVariableContext();

        variableContext.addLocalVariable("DEVELOCITY_INJECTION_ENABLED", "true");
        variableContext.addLocalVariable("DEVELOCITY_INJECTION_INIT_SCRIPT_NAME", GradleEmbeddedResources.INIT_SCRIPT_NAME);
        variableContext.addLocalVariable("DEVELOCITY_AUTO_INJECTION_CUSTOM_VALUE", "Bamboo");
        Objects.runIfNotNull(config.server, it -> variableContext.addLocalVariable("DEVELOCITY_URL", it));
        Objects.runIfTrue(config.enforceUrl, () -> variableContext.addLocalVariable("DEVELOCITY_ENFORCE_URL", "true"));
        Objects.runIfTrue(config.allowUntrustedServer, () -> variableContext.addLocalVariable("DEVELOCITY_ALLOW_UNTRUSTED_SERVER", "true"));
        Objects.runIfNotNull(config.develocityPluginVersion, it -> variableContext.addLocalVariable("DEVELOCITY_PLUGIN_VERSION", it));
        Objects.runIfNotNull(config.ccudPluginVersion, it -> variableContext.addLocalVariable("CCUD_PLUGIN_VERSION", it));
        Objects.runIfTrue(config.gradleCaptureFileFingerprints, () -> variableContext.addLocalVariable("DEVELOCITY_CAPTURE_FILE_FINGERPRINTS", "true"));

        Objects.runIfNotNull(config.pluginRepository, it -> variableContext.addLocalVariable("GRADLE_PLUGIN_REPOSITORY_URL", it));
        Objects.runIfNotNull(
                config.pluginRepositoryCredentialName,
                it -> {
                    UsernameAndPassword credentials = credentialsProvider.findByName(it).orElse(null);
                    if (credentials == null) {
                        LOGGER.warn("Plugin repository credentials with the name {} are not found.", it);
                    } else {
                        if (credentials.getUsername() == null || credentials.getPassword() == null) {
                            LOGGER.warn("Plugin repository credentials {} do not have username or password set.", it);
                        } else {
                            variableContext.addLocalVariable("GRADLE_PLUGIN_REPOSITORY_USERNAME", credentials.getUsername());
                            variableContext.addLocalVariable("GRADLE_PLUGIN_REPOSITORY_PASSWORD", credentials.getPassword());
                        }
                    }
                }
        );
    }
}
