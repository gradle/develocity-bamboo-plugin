package com.gradle.develocity.bamboo;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DevelocityAccessKeyTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "server=secret",
        "server=secret ",
        "server = secret",
        " server= secret",

        "sever1,server2,server3=secret",
        " sever1, server2 , server3 = secret ",

        "server1=secret1;server2=secret2;server3=secret3",
        " server1= secret1; server2 , sever3 = secret2 ;"
    })
    void validAccessKeys(String accessKey) {
        assertThat(DevelocityAccessKey.isValid(accessKey), is(true));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        " ",
        "server=",
        "=secret",
        "secret",
        "server=secret; ",
        ";server=secret",
        "server1, server2,, server3 = secret "
    })
    void invalidAccessKeys(String accessKey) {
        assertThat(DevelocityAccessKey.isValid(accessKey), is(false));
    }
}
