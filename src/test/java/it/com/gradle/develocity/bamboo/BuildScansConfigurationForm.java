package it.com.gradle.develocity.bamboo;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class BuildScansConfigurationForm {

    private final Page page;

    public BuildScansConfigurationForm(Page page) {
        this.page = page;
    }

    public BuildScansConfigurationForm clear() {
        Stream.of(getServerLocator(), getPluginRepositoryLocator(), getDevelocityPluginVersionLocator(), getCcudPluginVersionLocator())
            .forEach(Locator::clear);

        Stream.of(getAllowUntrustedServerLocator(), getInjectMavenExtensionLocator(), getInjectCcudExtensionLocator())
            .forEach(Locator::uncheck);

        Stream.of(getSharedCredentialNameLocator(), getPluginRepositoryCredentialNameLocator())
            .forEach(it -> it.selectOption("None"));

        return save();
    }

    public BuildScansConfigurationForm configure(Consumer<BuildScansConfigurationForm> configurator) {
        configurator.accept(this);
        return this;
    }

    public BuildScansConfigurationForm save() {
        page.locator("#saveBuildScansConfig_save").click();
        return this;
    }

    public BuildScansConfigurationForm setServer(String url) {
        getServerLocator().fill(url);
        return this;
    }

    public BuildScansConfigurationForm setSharedCredentialName(String name) {
        getSharedCredentialNameLocator().selectOption(name);
        return this;
    }

    public BuildScansConfigurationForm setServer(URI url) {
        return setServer(url.toString());
    }

    public BuildScansConfigurationForm setDevelocityPluginVersion(String version) {
        getDevelocityPluginVersionLocator().fill(version);
        return this;
    }

    public BuildScansConfigurationForm setCcudPluginVersion(String version) {
        getCcudPluginVersionLocator().fill(version);
        return this;
    }

    public BuildScansConfigurationForm setPluginRepository(String url) {
        getPluginRepositoryLocator().fill(url);
        return this;
    }

    public BuildScansConfigurationForm setPluginRepositoryCredentialName(String name) {
        getPluginRepositoryCredentialNameLocator().selectOption(name);
        return this;
    }

    public BuildScansConfigurationForm allowUntrustedServer() {
        getAllowUntrustedServerLocator().check();
        return this;
    }

    public BuildScansConfigurationForm enableDevelocityExtensionAutoInjection() {
        getInjectMavenExtensionLocator().check();
        return this;
    }

    public BuildScansConfigurationForm enableCcudExtensionAutoInjection() {
        getInjectCcudExtensionLocator().check();
        return this;
    }

    public BuildScansConfigurationForm setMavenExtensionCustomCoordinates(String coordinates) {
        getMavenExtensionCustomCoordinatesLocator().fill(coordinates);
        return this;
    }

    public BuildScansConfigurationForm enforceUrl() {
        getEnforceUrlLocator().check();
        return this;
    }

    public BuildScansConfigurationForm doNotEnforceUrl() {
        getEnforceUrlLocator().uncheck();
        return this;
    }

    public BuildScansConfigurationForm setVcsRepositoryFilter(String filter) {
        getVcsRepositoryFilterLocator().fill(filter);
        return this;
    }

    public BuildScansConfigurationForm setShortLivedTokenExpiry(String expiry) {
        getShortLivedTokenExpiry().fill(expiry);
        return this;
    }

    private Locator getVcsRepositoryFilterLocator() {
        return page.getByLabel("Auto-injection Git VCS repository filters");
    }

    public Locator locator(String selector) {
        return page.locator(selector);
    }

    public Locator getServerLocator() {
        return page.getByLabel("Develocity server URL", new Page.GetByLabelOptions().setExact(true));
    }

    public Locator getSharedCredentialNameLocator() {
        return page.getByLabel("Shared credential name");
    }

    public Locator getDevelocityPluginVersionLocator() {
        return page.getByLabel("Develocity Gradle plugin version");
    }

    public Locator getCcudPluginVersionLocator() {
        return page.getByLabel("Common Custom User Data Gradle plugin version");
    }

    public Locator getPluginRepositoryLocator() {
        return page.getByLabel("Gradle plugin repository URL");
    }

    public Locator getPluginRepositoryCredentialNameLocator() {
        return page.getByLabel("Gradle plugin repository credential name");
    }

    public Locator getAllowUntrustedServerLocator() {
        return page.getByText("Allow untrusted server");
    }

    public Locator getInjectMavenExtensionLocator() {
        return page.locator("#label_saveBuildScansConfig_injectMavenExtension");
    }

    public Locator getInjectCcudExtensionLocator() {
        return page.getByText("Enables Common Custom User Data Maven extension auto-injection");
    }

    public Locator getMavenExtensionCustomCoordinatesLocator() {
        return page.getByText("Develocity Maven Extension Custom Coordinates");
    }

    public Locator getEnforceUrlLocator() {
        return page.getByText("Enforce Develocity server URL");
    }

    public Locator getShortLivedTokenExpiry() {
        return page.getByText("Develocity short-lived access token expiry");
    }

}
