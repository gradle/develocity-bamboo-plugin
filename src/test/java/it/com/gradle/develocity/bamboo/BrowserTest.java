package it.com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.BambooApi;
import com.gradle.develocity.bamboo.model.TestUser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;
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
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setSlowMo(1000);

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

        BrowserContext browserContext = browser.newContext(contextOptions);
        browserContext.setDefaultTimeout(60000);

        return browserContext;
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    public final void loginAs(TestUser user) {
        page.navigate(BAMBOO);

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
        page.locator("#createSharedCredentials_save").evaluate("element => element.click()");;
        page.waitForResponse(
                response -> response.url().contains("createSharedCredentials") && response.request().method().equals("POST"),
                () -> {}
        );

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

        Locator whatsNewDialog = page.locator(".aui-dialog2-header-main:has-text('new')");

        if (whatsNewDialog.isVisible()) {
            page.locator("#fd__start-close-btn").click();

            page.locator(".aui-blanket").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        }

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
        return RandomStringUtils.secure().nextAlphanumeric(10);
    }

    private void gotoAdminPage() {
        page.navigate(BAMBOO + "/admin/administer.action");
    }

    private void gotoCredentialsPage() {
        Response navigate = page.navigate(BAMBOO + "/admin/credentials/configureSharedCredentials.action");
        if (navigate.status() == 404) {
            page.navigate(BAMBOO + "/admin/configureSharedCredentials.action");
        }
    }

}
