package it.com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.DevelocityAccessCredential;
import com.gradle.develocity.bamboo.ShortLivedTokenClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class ShortLivedTokenClientTest {

    @RegisterExtension
    public final MockDevelocityServer mockDevelocityServer = new MockDevelocityServer();

    private final ShortLivedTokenClient shortLivedTokenClient = new ShortLivedTokenClient();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2"})
    void shortLivedTokenIsRetrieved(String expiryInHours) {
        DevelocityAccessCredential develocityAccessCredential = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessCredential.parse("localhost=" + RandomStringUtils.randomAlphanumeric(30), "localhost").get(),
                expiryInHours
        ).orElseThrow(() -> new IllegalStateException("Short lived token value is expected"));

        assertThat(develocityAccessCredential.getHostname(), equalTo("localhost"));
        assertThat(develocityAccessCredential.getKey(), not(isEmptyOrNullString()));
    }

    @Test
    void shortLivedTokenIsNotRetrievedIfResponseIsNotSuccessful() {
        mockDevelocityServer.rejectShortLivedTokenCreation();

        Optional<DevelocityAccessCredential> develocityAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessCredential.parse("localhost=" + RandomStringUtils.randomAlphanumeric(30), "localhost").get(),
                null
        );

        assertThat(develocityAccessKey.isPresent(), is(false));
    }

    @Test
    void shortLivedTokenRetrievalFailsWithExceotion() {
        Optional<DevelocityAccessCredential> develocityAccessKey = shortLivedTokenClient.get(
                "http://localhost:8888",
                DevelocityAccessCredential.parse("localhost=" + RandomStringUtils.randomAlphanumeric(30), "localhost").get(),
                null
        );

        assertThat(develocityAccessKey.isPresent(), is(false));
    }

}
