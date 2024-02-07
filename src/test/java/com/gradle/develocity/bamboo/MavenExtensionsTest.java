package com.gradle.develocity.bamboo;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenExtensionsTest {

    private static final MavenCoordinates SOME_EXTENSION_COORDINATES = new MavenCoordinates("com.gradle", "some-artifact-id", "0.1.0");

    @TempDir
    private File extensionsDir;
    private File extensionsXml;

    @BeforeEach
    void setup() {
        extensionsXml = new File(extensionsDir, "extensions.xml");
    }

    @Test
    void hasExtensionReturnsFalseWhenMavenExtensionsIsCreatedViaEmpty() {
        MavenExtensions mavenExtensions = MavenExtensions.empty();

        assertFalse(mavenExtensions.hasExtension(SOME_EXTENSION_COORDINATES));
    }

    @Test
    void hasExtensionReturnsFalseWhenExtensionsXmlFileIsNotPresent() {
        extensionsXml.delete();

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertFalse(mavenExtensions.hasExtension(SOME_EXTENSION_COORDINATES));
    }

    @Test
    void hasExtensionReturnsFalseWhenExtensionsXmlFileIsNotValidXml() throws IOException {
        FileUtils.writeLines(extensionsXml, Collections.singleton(""));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertFalse(mavenExtensions.hasExtension(SOME_EXTENSION_COORDINATES));
    }

    @Test
    void hasExtensionReturnsFalseWhenCoordinatesArgumentIsNull() throws IOException {
        FileUtils.writeLines(extensionsXml, Collections.singleton(generateExtensionsXml(SOME_EXTENSION_COORDINATES)));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertFalse(mavenExtensions.hasExtension(null));
    }

    @Test
    void hasExtensionReturnsFalseWhenCoordinatesArgumentHasInvalidCharactersForXpathExpression() throws IOException {
        MavenCoordinates coordsWithInvalidCharacters = new MavenCoordinates("com.example", "\"");
        FileUtils.writeLines(extensionsXml, Collections.singleton(generateExtensionsXml(SOME_EXTENSION_COORDINATES)));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertFalse(mavenExtensions.hasExtension(coordsWithInvalidCharacters));
    }

    @Test
    void hasExtensionReturnsFalseWhenOnlyGroupOrArtifactOfCoordinatesArgumentMatches() throws IOException {
        MavenCoordinates coordsInExtensionsXml = new MavenCoordinates("com.gradle", "some-artifact-id", "0.1.0");
        MavenCoordinates coordsToMatchSameGroup = new MavenCoordinates("com.gradle", "different-artifact-d");
        MavenCoordinates coordsToMatchSameArtifact = new MavenCoordinates("com.different", "some-artifact-id");
        FileUtils.writeLines(extensionsXml, Collections.singleton(generateExtensionsXml(coordsInExtensionsXml)));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertFalse(mavenExtensions.hasExtension(coordsToMatchSameGroup));
        assertFalse(mavenExtensions.hasExtension(coordsToMatchSameArtifact));
    }

    @Test
    void  hasExtensionReturnsTrueWhenGroupAndArtifactOfCoordinatesArgumentMatches() throws IOException {
        MavenCoordinates coordinatesInExtensionsXml = new MavenCoordinates("com.gradle", "some-artifact-id", "0.1.0");
        MavenCoordinates coordinatesToMatch = new MavenCoordinates("com.gradle", "some-artifact-id", "0.2.0");
        FileUtils.writeLines(extensionsXml, Collections.singleton(generateExtensionsXml(coordinatesInExtensionsXml)));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertTrue(mavenExtensions.hasExtension(coordinatesToMatch));
    }

    @Test
    void hasExtensionReturnsTrueWhenGroupArtifactAndVersionOfCoordinatesArgumentMatches() throws IOException {
        MavenCoordinates coordinatesInExtensionsXml = new MavenCoordinates("com.example", "example-artifact-id", "0.1.0");
        FileUtils.writeLines(extensionsXml, Collections.singleton(generateExtensionsXml(SOME_EXTENSION_COORDINATES, coordinatesInExtensionsXml)));

        MavenExtensions mavenExtensions = MavenExtensions.fromFile(extensionsXml);

        assertTrue(mavenExtensions.hasExtension(SOME_EXTENSION_COORDINATES));
        assertTrue(mavenExtensions.hasExtension(coordinatesInExtensionsXml));
    }

    private static String generateExtensionsXml(MavenCoordinates... mavenCoordinates) {
        String extensions = Arrays.stream(mavenCoordinates)
                .map(it -> String.format("<extension> <groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version></extension>", it.groupId(), it.artifactId(), it.version()))
                .collect(Collectors.joining("\n"));

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><extensions>" + extensions + "</extensions>";
    }

}