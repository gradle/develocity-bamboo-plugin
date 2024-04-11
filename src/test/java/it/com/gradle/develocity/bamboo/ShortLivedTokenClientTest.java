package it.com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.DevelocityAccessKey;
import com.gradle.develocity.bamboo.ShortLivedTokenClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
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
        DevelocityAccessKey develocityAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessKey.of("localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                expiryInHours
        ).orElseThrow(() -> new IllegalStateException("Short lived token value is expected"));

        assertThat(develocityAccessKey.getHostname(), equalTo("localhost"));
        assertThat(develocityAccessKey.getKey(), not(isEmptyOrNullString()));
    }

    @Test
    void shortLivedTokenIsNotRetrievedIfResponseIsNotSuccessful() {
        mockDevelocityServer.rejectShortLivedTokenCreation();

        Optional<DevelocityAccessKey> develocityAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessKey.of("localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                null
        );

        assertThat(develocityAccessKey.isPresent(), is(false));
    }

    @Test
    void shortLivedTokenRetrievalFailsWithExceotion() {
        Optional<DevelocityAccessKey> develocityAccessKey = shortLivedTokenClient.get(
                "http://localhost:8888",
                DevelocityAccessKey.of("localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                null
        );

        assertThat(develocityAccessKey.isPresent(), is(false));
    }

}
