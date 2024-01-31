package it.com.gradle.develocity.bamboo.injection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ValueSource(strings = {
    "GPM", // Uses script task to invoke Gradle
    "GPCM" // Uses command task to invoke Gradle
})
public @interface GradleProjectTest {
}
