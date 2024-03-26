package com.gradle.develocity.bamboo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class MavenEmbeddedResources {

    private static final File DEFAULT_TARGET_DIRECTORY = new File(".develocity-plugin/maven");

    File copy(Resource resource) {
        return copy(resource, DEFAULT_TARGET_DIRECTORY);
    }

    File copy(Resource resource, File targetDirectory) {
        try {
            File targetFile = resource.getTargetFile(targetDirectory);
            if (!targetFile.exists()) {
                try (InputStream is = MavenEmbeddedResources.class.getResourceAsStream(resource.getName())) {
                    if (is == null) {
                        throw new IOException(String.format("Embedded resource %s not found", resource.getName()));
                    }

                    targetFile.getParentFile().mkdirs();

                    Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return targetFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    enum Resource {

        DEVELOCITY_EXTENSION(
            "develocity-maven-extension-" + Versions.DEVELOCITY_EXTENSION_VERSION + ".jar",
            "develocity-maven-extension.jar"
        ),

        CCUD_EXTENSION(
            "common-custom-user-data-maven-extension-" + Versions.CCUD_EXTENSION_VERSION + ".jar",
            "common-custom-user-data-maven-extension.jar"
        );

        private final String sourceFilename;
        private final String targetFilename;

        Resource(String sourceFilename, String targetFilename) {
            this.sourceFilename = sourceFilename;
            this.targetFilename = targetFilename;
        }

        String getName() {
            return String.format("/develocity/maven/%s", sourceFilename);
        }

        File getTargetFile(File targetDirectory) {
            return new File(targetDirectory, targetFilename);
        }
    }
}
