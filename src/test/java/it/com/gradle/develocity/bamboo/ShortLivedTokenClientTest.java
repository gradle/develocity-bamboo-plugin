package it.com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.DevelocityAccessCredentials;
import com.gradle.develocity.bamboo.ShortLivedTokenClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ratpack.test.embed.EmbeddedApp;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class ShortLivedTokenClientTest implements AfterEachCallback {

    private final ShortLivedTokenClient shortLivedTokenClient = new ShortLivedTokenClient();

    private EmbeddedApp mockDevelocityServer;

    @Override
    public void afterEach(ExtensionContext context) {
        if (mockDevelocityServer != null) {
            mockDevelocityServer.close();
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2"})
    void shortLivedTokenIsRetrieved(String expiryInHours) {
        String shortLivedToken = RandomStringUtils.randomAlphanumeric(50);
        mockDevelocityServer = EmbeddedApp.fromHandlers(
                c -> c.post("api/auth/token", ctx -> ctx.getResponse().status(200).send(shortLivedToken))
        );

        DevelocityAccessCredentials.HostnameAccessKey hostnameAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessCredentials.HostnameAccessKey.of("localhost", "localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                expiryInHours
        ).orElseThrow(() -> new IllegalStateException("Short lived token value is expected"));

        assertThat(hostnameAccessKey.getHostname(), equalTo("localhost"));
        assertThat(hostnameAccessKey.getKey(), equalTo(shortLivedToken));
    }

    @Test
    void shortLivedTokenIsNotRetrievedIfResponseIsNotSuccessful() {
        AtomicInteger requestCount = new AtomicInteger(0);
        mockDevelocityServer = EmbeddedApp.fromHandlers(
                c -> c.post("api/auth/token", ctx -> {
                    requestCount.incrementAndGet();
                    ctx.getResponse().status(503);
                })
        );

        Optional<DevelocityAccessCredentials.HostnameAccessKey> hostnameAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessCredentials.HostnameAccessKey.of("localhost", "localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                null
        );

        assertThat(hostnameAccessKey.isPresent(), is(false));
        assertThat(requestCount.get(), equalTo(3));
    }

    @Test
    void shortLivedTokenRetrievalFailsWithException() {
        Optional<DevelocityAccessCredentials.HostnameAccessKey> hostnameAccessKey = shortLivedTokenClient.get(
                "http://localhost:8888",
                DevelocityAccessCredentials.HostnameAccessKey.of("localhost", "localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                null
        );

        assertThat(hostnameAccessKey.isPresent(), is(false));
    }

    @Test
    void shortLivedTokenRetrievalSuccedsAfterRetry() {
        AtomicBoolean firstRequest = new AtomicBoolean(true);
        mockDevelocityServer = EmbeddedApp.fromHandlers(
                c -> c.post("api/auth/token", ctx -> {
                    if (firstRequest.get()) {
                        firstRequest.set(false);
                        ctx.getResponse().status(503);
                    } else {
                        ctx.getResponse().status(200).send(RandomStringUtils.randomAlphanumeric(50));
                    }
                })
        );
        DevelocityAccessCredentials.HostnameAccessKey hostnameAccessKey = shortLivedTokenClient.get(
                mockDevelocityServer.getAddress().toString(),
                DevelocityAccessCredentials.HostnameAccessKey.of("localhost", "localhost=" + RandomStringUtils.randomAlphanumeric(30)),
                null
        ).orElseThrow(() -> new IllegalStateException("Short lived token value is expected"));

        assertThat(hostnameAccessKey.getHostname(), equalTo("localhost"));
        assertThat(hostnameAccessKey.getKey(), not(isEmptyOrNullString()));
    }

}
