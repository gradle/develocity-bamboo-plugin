package it.com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.BambooApi;
import com.gradle.develocity.bamboo.model.TestUser;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Paths;
import java.util.function.Consumer;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * @see <a href="https://playwright.dev/java/docs/test-runners">https://playwright.dev/java/docs/test-runners</a>
 */
public abstract class BrowserTest {

    public static final String BAMBOO = "http://localhost:6990/bamboo";

    private static final String VIDEO_RECORDING_ENABLED = "VIDEO_RECORDING_ENABLED";
    private static final String HEADLESS_BROWSER_DISABLED = "HEADLESS_BROWSER_DISABLED";

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    protected static BambooApi bambooApi;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = launch(playwright.chromium());
    }

    @BeforeAll
    static void createBambooApi() {
        bambooApi = new BambooApi(BAMBOO, TestUser.ADMIN);
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @AfterAll
    static void tearDownBambooApi() {
        bambooApi.close();
    }

    @BeforeEach
    public void createContextAndPage() {
        context = createBrowserContext();
        page = context.newPage();
    }

    private static Browser launch(BrowserType browserType) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setSlowMo(100);

        if (BooleanUtils.toBoolean(System.getenv(HEADLESS_BROWSER_DISABLED))) {
            launchOptions.setHeadless(false);
        }

        return browserType.launch(launchOptions);
    }

    private BrowserContext createBrowserContext() {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions();

        if (BooleanUtils.toBoolean(System.getenv(VIDEO_RECORDING_ENABLED))) {
            contextOptions
                    .setRecordVideoDir(Paths.get("target/playwright/videos"))
                    .setRecordVideoSize(1024, 768);
        }

        return browser.newContext(contextOptions);
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    public final void loginAs(TestUser user) {
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                page.navigate(BAMBOO, new Page.NavigateOptions().setTimeout(90000));
                break;
            } catch (PlaywrightException e) {
                if (e.getMessage().contains("ERR_ABORTED")) {
                    if (attempt == maxRetries) {
                        throw new RuntimeException("Failed to load Admin page after " + maxRetries + " attempts.", e);
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }

                } else {
                    throw e;
                }
            }
        }

        // Login
        page.locator("#login").click();
        page.locator("#username-field").fill(user.getUsername());
        page.locator("#password-field").fill(user.getPassword());
        page.locator("#login-button").click();
    }

    public final String storePluginCredentialInSharedCredentials(String username, String password) {
        return storeSharedCredential(username, password);
    }

    public final String storeAccessKeyInSharedCredentials(@Nullable String accessKey) {
        return storeSharedCredential("develocity", accessKey);
    }

    private String storeSharedCredential(String username, String password) {
        gotoCredentialsPage();

        String credentialsName = randomString();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add new credentials")).click();

        page.locator("#credentials-dropdown2-select").getByText("Username and password").click();

        page.getByLabel("Credential name (required)").fill(credentialsName);
        page.getByLabel("Username (required)").fill(username);
        if (password != null) {
            page.getByLabel("Password").fill(password);
        }
        page.locator("#createSharedCredentials_save").click();
        page.reload();

        return credentialsName;
    }

    public final void setupMaven3(String mavenHome) {
        openSharedRemoteCapabilities();

        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName("Maven 3 (Maven 3.x)")).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit")).click();
        page.getByLabel("Path").fill(mavenHome);
        page.locator("#updateSharedCapability_save").click();
    }

    public final void setupJavaHome(String javaHome) {
        openSharedRemoteCapabilities();

        page.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName("JDK 17")).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Edit")).click();
        page.getByLabel("Java home").fill(javaHome);
        page.locator("#updateSharedCapability_save").click();
    }

    public final void openSharedRemoteCapabilities() {
        gotoAdminPage();

        page.locator("#admin-menu").getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Agents")).click();
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Shared remote capabilities")).click();
    }

    public final void ensurePluginConfiguration(Consumer<BuildScansConfigurationForm> configurator) {
        assertPluginConfiguration(
                configurator,
                form -> assertThat(form.locator("div.error.control-form-error")).not().isVisible()
        );
    }

    public final void assertPluginConfiguration(Consumer<BuildScansConfigurationForm> configurator,
                                                Consumer<BuildScansConfigurationForm> assertions) {
        gotoAdminPage();

        // Select build scan injection
        page.locator("#configureBuildScans").click();

        BuildScansConfigurationForm form =
                new BuildScansConfigurationForm(page)
                        .clear()
                        .configure(configurator)
                        .save();

        assertions.accept(form);
    }

    public final String randomString() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private void gotoAdminPage() {
        page.navigate(BAMBOO + "/admin/administer.action", new Page.NavigateOptions().setTimeout(90000));
    }

    private void gotoCredentialsPage() {
        page.navigate(BAMBOO + "/admin/credentials/configureSharedCredentials.action", new Page.NavigateOptions().setTimeout(90000));
    }

}
