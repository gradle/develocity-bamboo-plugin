package it.com.gradle.develocity.bamboo.browser;

import com.gradle.develocity.bamboo.Versions;
import com.gradle.develocity.bamboo.model.TestUser;
import com.microsoft.playwright.Locator;
import it.com.gradle.develocity.bamboo.BrowserTest;
import it.com.gradle.develocity.bamboo.BuildScansConfigurationForm;
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
        String sharedCredentialName = storeAccessKeyInSharedCredentials(String.format("scans.gradle.com=%s", randomString()));
        String pluginRepositoryCredentialName = storePluginCredentialInSharedCredentials(randomString(), randomString());

        assertPluginConfiguration(
            form -> form
                .setServer("https://scans.gradle.com")
                .setSharedCredentialName(sharedCredentialName)
                .setDevelocityPluginVersion("3.12")
                .setCcudPluginVersion("1.8.2")
                .setPluginRepository("https://plugins.gradle.org")
                .setPluginRepositoryCredentialName(pluginRepositoryCredentialName)
                .allowUntrustedServer()
                .enforceUrl()
                .setShortLivedTokenExpiry("6")
                .enableDevelocityExtensionAutoInjection()
                .enableCcudExtensionAutoInjection(),

            form -> {
                assertThat(form.getServerLocator()).hasValue("https://scans.gradle.com");
                assertThat(form.getSharedCredentialNameLocator()).hasValue(sharedCredentialName);
                assertThat(form.getDevelocityPluginVersionLocator()).hasValue("3.12");
                assertThat(form.getCcudPluginVersionLocator()).hasValue("1.8.2");
                assertThat(form.getPluginRepositoryLocator()).hasValue("https://plugins.gradle.org");
                assertThat(form.getPluginRepositoryCredentialNameLocator()).hasValue(pluginRepositoryCredentialName);
                assertThat(form.getShortLivedTokenExpiry()).hasValue("6");

                assertThat(form.getAllowUntrustedServerLocator()).isChecked();
                assertThat(form.getEnforceUrlLocator()).isChecked();
                assertThat(form.getInjectMavenExtensionLocator()).isChecked();
                assertThat(form.getInjectCcudExtensionLocator()).isChecked();
            }
        );
    }

    @Test
    void invalidServerUrl() {
        assertInvalidInput(
            form -> form.setServer(randomString()),
            "#fieldArea_saveBuildScansConfig_server > div.error.control-form-error",
            "Please specify a valid URL of the Develocity server."
        );
    }

    @Test
    void invalidSharedCredential() {
        String sharedCredentialName = storeAccessKeyInSharedCredentials(randomString());

        assertInvalidInput(
            form -> form.setSharedCredentialName(sharedCredentialName),
            "#fieldArea_saveBuildScansConfig_sharedCredentialName > div.error.control-form-error",
            "Shared credential contains an invalid access key."
        );
    }

    @Test
    void sharedCredentialWithoutPassword() {
        String sharedCredentialName = storeAccessKeyInSharedCredentials(null);

        assertInvalidInput(
            form -> form.setSharedCredentialName(sharedCredentialName),
            "#fieldArea_saveBuildScansConfig_sharedCredentialName > div.error.control-form-error",
            "Shared credential contains an invalid access key."
        );
    }

    @Test
    void invalidDevelocityPluginVersion() {
        assertInvalidInput(
            form -> form.setDevelocityPluginVersion(randomString()),
            "#fieldArea_saveBuildScansConfig_develocityPluginVersion > div.error.control-form-error",
            "Please specify a valid version of the Develocity Gradle plugin."
        );
    }

    @Test
    void invalidCcudPluginVersion() {
        assertInvalidInput(
            form -> form.setCcudPluginVersion(randomString()),
            "#fieldArea_saveBuildScansConfig_ccudPluginVersion > div.error.control-form-error",
            "Please specify a valid version of the Common Custom User Data Gradle plugin."
        );
    }

    @Test
    void invalidGradlePluginRepository() {
        assertInvalidInput(
            form -> form.setPluginRepository(randomString()),
            "#fieldArea_saveBuildScansConfig_pluginRepository > div.error.control-form-error",
            "Please specify a valid URL of the Gradle plugins repository."
        );
    }

    @Test
    void invalidShortLivedTokenExpiry() {
        assertInvalidInput(
            form -> form.setShortLivedTokenExpiry(randomString()),
            "#fieldArea_saveBuildScansConfig_shortLivedTokenExpiry > div.error.control-form-error",
            "Please specify a valid short-lived token expiry in hours between 1 and 24, i.e. 6"
        );
    }

    @Test
    void showsEmbeddedDevelocityExtensionVersion() {
        assertPluginConfiguration(
            NO_OP_CONFIGURATOR,
            form ->
                assertThat(form.locator("#saveBuildScansConfig_injectMavenExtensionDesc"))
                    .hasText("Injects the Develocity Maven extension " + Versions.DEVELOCITY_EXTENSION_VERSION + " to Maven builds."));
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
