[#-- @ftlvariable name="" type="com.gradle.enterprise.bamboo.admin.BuildScansConfigAction" --]
<html>
<head>
    <title>Gradle Enterprise integration</title>
    <meta name="decorator" content="adminpage">
</head>

<body>

[@ww.form action="/admin/saveBuildScansConfig.action" submitLabelKey="global.buttons.update" cancelUri="/admin/buildScansConfig.action" showActionErrors="true"]
    <div class="paddedClearer"></div>

    [@ui.bambooSection titleKey="gradle-enterprise.config.connection-settings.title" headerWeight="h2"]
        [@ww.textfield labelKey="gradle-enterprise.config.server" name="server" autofocus=true/]
        [@ww.checkbox labelKey="gradle-enterprise.config.allow-untrusted-server" name="allowUntrustedServer" toggle="true"/]
        [@ww.textfield labelKey="gradle-enterprise.config.shared-credential-name" name="sharedCredentialName"/]
        <div class="field-group">
            <div style="display: inline-block;">
                [@ui.messageBox type="info"]
                    <div>
                        The access key must be in the <b>&lt;server host name&gt;=&lt;access key&gt;</b> format. For more
                        details please refer to the <a href="https://docs.gradle.com/enterprise/gradle-plugin/#manual_access_key_configuration" target="_blank">documentation</a>.
                    </div>
                [/@ui.messageBox]
            </div>
        </div>
    [/@ui.bambooSection]

    [@ui.bambooSection titleKey="gradle-enterprise.config.gradle-settings.title" headerWeight="h2"]
        [@ww.textfield labelKey="gradle-enterprise.config.ge-plugin.version" name="gePluginVersion"/]
        [@ww.textfield labelKey="gradle-enterprise.config.ccud-plugin.version" name="ccudPluginVersion"/]
        [@ww.textfield labelKey="gradle-enterprise.config.plugin-repository" name="pluginRepository"/]
    [/@ui.bambooSection]

    [@ui.bambooSection titleKey="gradle-enterprise.config.maven-settings.title" headerWeight="h2"]
        [@ww.checkbox labelKey="gradle-enterprise.config.maven-extension.enabled" name="injectMavenExtension" toggle="true"/]
        [@ww.checkbox labelKey="gradle-enterprise.config.ccud-extension.enabled" name="injectCcudExtension" toggle="true"/]
    [/@ui.bambooSection]
[/@ww.form]

</body>
</html>
