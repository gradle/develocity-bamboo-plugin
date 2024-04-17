[#-- @ftlvariable name="" type="com.gradle.develocity.bamboo.admin.BuildScansConfigAction" --]
<html>
<head>
    <title>Develocity integration</title>
    <meta name="decorator" content="adminpage">
</head>

<body>

[@ww.form action="/admin/saveBuildScansConfig.action" submitLabelKey="global.buttons.update" cancelUri="/admin/buildScansConfig.action" showActionErrors="true"]
    <div style="display: inline-block;">
        [@ui.messageBox type="info"]
            [@ww.text name="develocity.config.auto-injection-info"/]
        [/@ui.messageBox]
    </div>
    <div class="paddedClearer"></div>
    [@ui.bambooSection titleKey="develocity.config.connection-settings.title" headerWeight="h2"]
        [@ww.textfield labelKey="develocity.config.server" name="server" autofocus=true/]
        [@ww.checkbox labelKey="develocity.config.allow-untrusted-server" name="allowUntrustedServer" toggle="true"/]
        [@ww.checkbox labelKey="develocity.config.enforce-url" name="enforceUrl" toggle="true"/]
        [@ww.select labelKey='develocity.config.shared-credential-name' name='sharedCredentialName'
            toggle='true'
            list=usernameAndPasswordCredentialNames
            listKey='name'
            listValue='label']
        [/@ww.select]
        <div class="field-group">
            <div style="display: inline-block;">
                [@ui.messageBox type="info"]
                    [@ww.text name="develocity.config.access-key-info"/]
                [/@ui.messageBox]
            </div>
        </div>
    [/@ui.bambooSection]
    [@ui.bambooSection titleKey="develocity.config.general.title" headerWeight="h2"]
        [@ww.textfield labelKey="develocity.config.general.short-lived-token-expiry" name="shortLivedTokenExpiry"/]
        [@ww.textarea labelKey='develocity.config.general.vcs-repository-filter' name='vcsRepositoryFilter' /]
        <div class="field-group">
            <div style="display: inline-block;">
                [@ui.messageBox type="info"]
                    [@ww.text name="develocity.config.general.vcs-repository-filter.info"/]
                [/@ui.messageBox]
            </div>
        </div>
    [/@ui.bambooSection]
    [@ui.bambooSection titleKey="develocity.config.gradle-settings.title" headerWeight="h2"]
        [@ww.textfield labelKey="develocity.config.develocity-plugin.version" name="develocityPluginVersion"/]
        [@ww.textfield labelKey="develocity.config.ccud-plugin.version" name="ccudPluginVersion"/]
        [@ww.textfield labelKey="develocity.config.plugin-repository" name="pluginRepository"/]
        [@ww.select labelKey='develocity.config.plugin-repository-credential-name' name='pluginRepositoryCredentialName'
            toggle='true'
            list=usernameAndPasswordCredentialNames
            listKey='name'
            listValue='label']
        [/@ww.select]
        [@ww.checkbox labelKey="develocity.config.gradle-capture-file-fingerprints.enabled" name="gradleCaptureFileFingerprints" toggle="true" value="true"/]
    [/@ui.bambooSection]

    [@ui.bambooSection titleKey="develocity.config.maven-settings.title" headerWeight="h2"]
        [@ww.checkbox labelKey="develocity.config.maven-extension.enabled" name="injectMavenExtension" toggle="true"/]
        [@ww.checkbox labelKey="develocity.config.ccud-extension.enabled" name="injectCcudExtension" toggle="true"/]
        [@ww.checkbox labelKey="develocity.config.maven-capture-file-fingerprints.enabled" name="mavenCaptureFileFingerprints" toggle="true" value="true"/]
        [@ww.textfield labelKey="develocity.config.custom-maven-extension" name="mavenExtensionCustomCoordinates"/]
        [@ww.textfield labelKey="develocity.config.custom-ccud-extension" name="ccudExtensionCustomCoordinates"/]
    [/@ui.bambooSection]
[/@ww.form]

</body>
</html>
