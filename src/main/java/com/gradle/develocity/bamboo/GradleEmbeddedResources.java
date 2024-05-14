package com.gradle.develocity.bamboo;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class GradleEmbeddedResources {

    public static final String INIT_SCRIPT_NAME = "develocity-init-script.gradle";

    File copyInitScript(String home) {
        try (InputStream is = GradleEmbeddedResources.class.getResourceAsStream(String.format("/develocity/gradle/%s", INIT_SCRIPT_NAME))) {
            if (is == null) {
                throw new IOException("Embedded Develocity init script not found");
            }

            File initScript = initScript(home);

            initScript.getParentFile().mkdirs();

            Files.copy(is, initScript.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return initScript;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void deleteInitScript(String home) {
        File initScript = initScript(home);
        if (initScript.exists()) {
            FileUtils.deleteQuietly(initScript);
        }
    }

    private static File initScript(String home) {
        return new File(home, String.format(".gradle/init.d/%s", INIT_SCRIPT_NAME));
    }
}
