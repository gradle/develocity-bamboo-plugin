package com.gradle.develocity.bamboo;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.File;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MavenEmbeddedResourcesTest {

    @TempDir
    Path folder;

    private final MavenEmbeddedResources mavenEmbeddedResources = new MavenEmbeddedResources();

    /**
     * Expected checksums can be found at:
     * https://repo1.maven.org/maven2/com/gradle/develocity-maven-extension/<version>/develocity-maven-extension-<version>.jar.md5
     * https://repo1.maven.org/maven2/com/gradle/common-custom-user-data-maven-extension/<version>/common-custom-user-data-maven-extension-<version>.jar.md5
     * https://repo1.maven.org/maven2/com/gradle/common-custom-user-data-maven-extension/<version>/common-custom-user-data-maven-extension-<version>.jar.md5
     */
    @ParameterizedTest
    @CsvSource({
            "DEVELOCITY_EXTENSION, 93f6aae3ad4b3cf5cf39c588951047bd",
            "CCUD_EXTENSION, 976c5592e4d7035a785aab2b8eb13610"
    })
    void copiesEmbeddedExtension(MavenEmbeddedResources.Resource resource, String expectedChecksum) throws Exception {
        Path tmp = folder.resolve("extensions");
        File targetDirectory = new File(tmp.toFile(), RandomStringUtils.randomAlphanumeric(10));

        File extension = mavenEmbeddedResources.copy(resource, targetDirectory);
        assertThat(extension.exists(), is(true));

        HashCode checksum = Files.asByteSource(extension).hash(Hashing.md5());
        assertThat(checksum.toString(), is(equalTo(expectedChecksum)));
    }
}
