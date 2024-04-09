package com.gradle.develocity.bamboo;

import org.springframework.stereotype.Component;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ShortLivedTokenClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortLivedTokenClient.class);

    private static final RequestBody EMPTY_BODY = RequestBody.create(new byte[]{});

    private final OkHttpClient httpClient;

    public ShortLivedTokenClient() {
        this.httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public Optional<DevelocityAccessKey> get(String server, DevelocityAccessKey accessKey) {
        String url = server + "/api/auth/token";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessKey.getKey())
                .addHeader("Content-Type", "application/json")
                .post(EMPTY_BODY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 200 && response.body() != null) {
                return Optional.of(DevelocityAccessKey.of(accessKey.getHostname(), response.body().string()));
            } else {
                LOGGER.warn("Develocity short lived token request failed {} with status code {}", url, response.code());
                return Optional.empty();
            }
        } catch (IOException e) {
            LOGGER.warn("Short lived token request failed {}", url, e);
            return Optional.empty();
        }
    }

}
