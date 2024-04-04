package com.gradle.develocity.bamboo;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;


class VcsRepositoryFilterTest {

    @ParameterizedTest
    @MethodSource("provideFilters")
    void filterMatchesUrl(String filter, String url, VcsRepositoryFilter.Result result) {
        MatcherAssert.assertThat(VcsRepositoryFilter.of(filter).matches(url), Matchers.is(result));
    }

    private static Stream<Arguments> provideFilters() {
        return Stream.of(
            Arguments.of("test", null, VcsRepositoryFilter.Result.NOT_MATCHED),
            Arguments.of(null, "http://foo", VcsRepositoryFilter.Result.NOT_MATCHED),
            Arguments.of("", "http://foo", VcsRepositoryFilter.Result.NOT_MATCHED),
            Arguments.of("test", "http://test", VcsRepositoryFilter.Result.NOT_MATCHED),
            Arguments.of("\n \n+: foo\n-:bar", "http://foo", VcsRepositoryFilter.Result.INCLUDED),
            Arguments.of("+:\n+: \n-:\n-: ", "http://foo", VcsRepositoryFilter.Result.NOT_MATCHED),
            Arguments.of(":foo \n-:bar ", "http://bar", VcsRepositoryFilter.Result.EXCLUDED),
            Arguments.of("+:one-inclusion", "http://one-inclusion/foo", VcsRepositoryFilter.Result.INCLUDED),
            Arguments.of("+:one-inclusion\n+:second-inclusion", "http://second-inclusion/foo", VcsRepositoryFilter.Result.INCLUDED),
            Arguments.of("-:one-exclusion", "http://one-exclusion/foo", VcsRepositoryFilter.Result.EXCLUDED),
            Arguments.of("-:one-exclusion\n-:second-exclusion", "http://second-exclusion/foo", VcsRepositoryFilter.Result.EXCLUDED),
            Arguments.of("+:one-inclusion\n-:one-exclusion", "http://one-inclusion/one-exclusion", VcsRepositoryFilter.Result.EXCLUDED),
            Arguments.of("+:one-inclusion\n+:second-inclusion\n-:one-exclusion\n-:second-exclusion", "http://one-inclusion/second-exclusion", VcsRepositoryFilter.Result.EXCLUDED),
            Arguments.of("+:one-inclusion\n-:one-exclusion\n+:second-inclusion\n-:second-exclusion", "http://one-inclusion/second-exclusion", VcsRepositoryFilter.Result.EXCLUDED)
        );
    }

}
