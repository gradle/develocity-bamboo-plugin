package com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.utils.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ShortLivedTokenClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortLivedTokenClient.class);

    private static final RequestBody EMPTY_BODY = RequestBody.create(new byte[]{});

    private static final int MAX_RETRIES = 3;
    private static final Duration RETRY_INTERVAL = Duration.ofSeconds(2);

    private final OkHttpClient httpClient;

    public ShortLivedTokenClient() {
        this.httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public Optional<DevelocityAccessCredential> get(String server, DevelocityAccessCredential accessKey, String expiryInHours) {
        String url = normalize(server) + "api/auth/token";
        if (StringUtils.isNotBlank(expiryInHours)) {
            url += "?expiresInHours=" + expiryInHours;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessKey.getKey())
                .addHeader("Content-Type", "application/json")
                .post(EMPTY_BODY)
                .build();

        long tryCount = 0;
        Integer errorCode = null;
        while (tryCount < MAX_RETRIES) {
            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 200 && response.body() != null) {
                    return Optional.of(DevelocityAccessCredential.of(accessKey.getHostname(), response.body().string()));
                } else {
                    tryCount++;
                    errorCode = response.code();
                    Objects.run(() -> Thread.sleep(RETRY_INTERVAL.toMillis()));
                }
            } catch (IOException e) {
                LOGGER.warn("Short lived token request failed {}", url, e);
                return Optional.empty();
            }
        }

        LOGGER.warn("Develocity short lived token request failed {} with status code {}", url, errorCode);
        return Optional.empty();
    }

    private static String normalize(String server) {
        return server.endsWith("/") ? server : server + "/";
    }

}
