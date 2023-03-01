package it.com.gradle.enterprise.bamboo.browser;

import com.gradle.enterprise.bamboo.Versions;
import com.gradle.enterprise.bamboo.model.TestUser;
import com.microsoft.playwright.Locator;
import it.com.gradle.enterprise.bamboo.BrowserTest;
import it.com.gradle.enterprise.bamboo.BuildScansConfigurationForm;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PluginConfigurationBrowserTest extends BrowserTest {

    // @formatter:off
    private static final Consumer<BuildScansConfigurationForm> NO_OP_CONFIGURATOR = form -> {};
    // @formatter:on

    @BeforeEach
    void login() {
        loginAs(TestUser.ADMIN);
    }

    @Test
    void shouldConfigureAllFields() {
        assertPluginConfiguration(
            form -> form
                .setServer("https://scans.gradle.com")
                .setGePluginVersion("3.12")
                .setCcudPluginVersion("1.8.2")
                .setPluginRepository("https://plugins.gradle.org")
                .allowUntrustedServer()
                .enableGeExtensionAutoInjection()
                .enableCcudExtensionAutoInjection(),

            form -> {
                assertThat(form.getServerLocator()).hasValue("https://scans.gradle.com");
                assertThat(form.getGePluginVersionLocator()).hasValue("3.12");
                assertThat(form.getCcudPluginVersionLocator()).hasValue("1.8.2");
                assertThat(form.getPluginRepositoryLocator()).hasValue("https://plugins.gradle.org");

                assertThat(form.getAllowUntrustedServerLocator()).isChecked();
                assertThat(form.getInjectMavenExtensionLocator()).isChecked();
                assertThat(form.getInjectCcudExtensionLocator()).isChecked();
            }
        );
    }

    @Test
    void invalidServerUrl() {
        assertInvalidInput(
            form -> form.setServer(RandomStringUtils.randomAscii(10)),
            "#fieldArea_saveBuildScansConfig_server > div.error.control-form-error",
            "Please specify a valid URL of the Gradle Enterprise server."
        );
    }

    @Test
    void sharedCredentialDoesNotExist() {
        assertInvalidInput(
            form -> form.setSharedCredentialName(RandomStringUtils.randomAscii(10)),
            "#fieldArea_saveBuildScansConfig_sharedCredentialName > div.error.control-form-error",
            "Please specify the name of the existing shared credential of type 'Username and password'."
        );
    }

    @Test
    void invalidGradleEnterprisePluginVersion() {
        assertInvalidInput(
            form -> form.setGePluginVersion(RandomStringUtils.randomAscii(10)),
            "#fieldArea_saveBuildScansConfig_gePluginVersion > div.error.control-form-error",
            "Please specify a valid version of the Gradle Enterprise Gradle plugin."
        );
    }

    @Test
    void invalidCcudPluginVersion() {
        assertInvalidInput(
            form -> form.setCcudPluginVersion(RandomStringUtils.randomAscii(10)),
            "#fieldArea_saveBuildScansConfig_ccudPluginVersion > div.error.control-form-error",
            "Please specify a valid version of the Common Custom User Data Gradle plugin."
        );
    }

    @Test
    void invalidGradlePluginRepository() {
        assertInvalidInput(
            form -> form.setPluginRepository(RandomStringUtils.randomAscii(10)),
            "#fieldArea_saveBuildScansConfig_pluginRepository > div.error.control-form-error",
            "Please specify a valid URL of the Gradle plugins repository."
        );
    }

    @Test
    void showsEmbeddedGradleEnterpriseExtensionVersion() {
        assertPluginConfiguration(
            NO_OP_CONFIGURATOR,
            form ->
                assertThat(form.locator("#saveBuildScansConfig_injectMavenExtensionDesc"))
                    .hasText("Injects the Gradle Enterprise Maven extension " + Versions.GE_EXTENSION_VERSION + " to Maven builds."));
    }

    @Test
    void showsEmbeddedCcudExtensionVersion() {
        assertPluginConfiguration(
            NO_OP_CONFIGURATOR,
            form ->
                assertThat(form.locator("#saveBuildScansConfig_injectCcudExtensionDesc"))
                    .hasText("Injects the Common Custom User Data Maven extension " + Versions.CCUD_EXTENSION_VERSION + " to Maven builds."));
    }

    private void assertInvalidInput(Consumer<BuildScansConfigurationForm> configurator,
                                    String outputSelector,
                                    String assertionText) {
        assertPluginConfiguration(
            configurator,
            form -> {
                Locator locator = form.locator(outputSelector);

                assertThat(locator).isVisible();
                assertThat(locator).hasText(assertionText);
            }
        );
    }
}
