package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanResultKey;
import com.atlassian.bamboo.util.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gradle.develocity.bamboo.model.Agent;
import com.gradle.develocity.bamboo.model.BuildResultDetails;
import com.gradle.develocity.bamboo.model.Jobs;
import com.gradle.develocity.bamboo.model.TestUser;
import com.gradle.develocity.bamboo.model.TriggeredBuild;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class BambooApi implements AutoCloseable {

    private static final String BAMBOO_AGENT_URL_PATTERN = "https://packages.atlassian.com/repository/public/com/atlassian/bamboo/bamboo-agent/%1$s/bamboo-agent-%1$s.jar";

    private static final Version BAMBOO_6_10_2 = new Version(6, 10, 2);

    private final String bambooUrl;
    private final CloseableHttpClient client;
    private final Supplier<HttpClientContext> authContext;
    private final Gson gson;

    private final AtomicReference<Version> cachedBambooVersion = new AtomicReference<>();

    public BambooApi(String bambooUrl, TestUser user) {
        this.bambooUrl = bambooUrl;
        this.client =
            HttpClients.custom()
                .setDefaultHeaders(
                    ImmutableList.of(
                        new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType())))
                .build();

        CredentialsProvider credentialsProvider = basicCredentialsProvider(user);
        AuthCache authCache = basicAuthCache(bambooUrl);

        this.authContext = () -> {
            HttpClientContext context = HttpClientContext.create();

            context.setCredentialsProvider(credentialsProvider);
            context.setAuthCache(authCache);

            return context;
        };

        this.gson = new GsonBuilder().create();
    }

    private static CredentialsProvider basicCredentialsProvider(TestUser user) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
            AuthScope.ANY, new UsernamePasswordCredentials(user.getUsername(), user.getPassword()));

        return credentialsProvider;
    }

    private static AuthCache basicAuthCache(String bambooUrl) {
        try {
            URL url = new URL(bambooUrl);

            AuthCache authCache = new BasicAuthCache();
            authCache.put(
                new HttpHost(url.getHost(), url.getPort(), url.getProtocol()), new BasicScheme());

            return authCache;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public Version getBambooVersion() {
        Version cachedVersion = cachedBambooVersion.get();
        if (cachedVersion != null) {
            return cachedVersion;
        }

        HttpGet request = new HttpGet(String.format("%s/rest/api/latest/info", bambooUrl));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to get Bamboo version");
            }

            String body = EntityUtils.toString(response.getEntity());

            // @formatter:off
            Map<String, String> json = gson.fromJson(body, new TypeToken<Map<String, String>>() {}.getType());
            // @formatter:on

            Version version =
                Optional.ofNullable(json.get("version"))
                    .map(Version::of)
                    .orElseThrow(() -> new IllegalStateException("Response must contain Bamboo version"));

            cachedBambooVersion.set(version);

            return version;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getLog(PlanResultKey planResultKey) {
        HttpGet request =
            new HttpGet(String.format("%s/download/%s/build_logs/%s.log", bambooUrl, planResultKey.getPlanKey().getKey(), planResultKey.getKey()));
        try (CloseableHttpResponse response = client.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to get build logs for " + planResultKey.getKey());
            }

            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public File downloadAgentJar() {
        Version bambooVersion = getBambooVersion();
        try {
            File tmp = Files.createTempDirectory("bambooAgent").toFile();
            File tmpAgentJar = new File(tmp, String.format("bamboo-agent-%s.jar", bambooVersion));

            FileUtils.copyURLToFile(
                new URL(String.format(BAMBOO_AGENT_URL_PATTERN, bambooVersion)),
                tmpAgentJar
            );

            return tmpAgentJar;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Collection<Agent> getAgents() {
        HttpGet request = new HttpGet(String.format("%s/rest/api/latest/agent", bambooUrl));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to get agents");
            }

            String body = EntityUtils.toString(response.getEntity());

            // @formatter:off
            return gson.fromJson(body, new TypeToken<List<Agent>>() {}.getType());
            // @formatter:on
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean deleteAgentSupported() {
        return getBambooVersion().isGreaterOrEqualTo(BAMBOO_6_10_2);
    }

    public void deleteAgent(long agentId) {
        HttpDelete request = new HttpDelete(String.format("%s/rest/api/latest/agent/%d", bambooUrl, agentId));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 204) {
                throw new ApiException(statusCode, "Unable to delete agent " + agentId);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public TriggeredBuild triggerBuild(PlanKey planKey) {
        HttpPost request = new HttpPost(String.format("%s/rest/api/1.0/queue/%s", bambooUrl, planKey.getKey()));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to trigger build");
            }

            String body = EntityUtils.toString(response.getEntity());

            return gson.fromJson(body, TriggeredBuild.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public BuildResultDetails getBuildResultDetails(PlanResultKey planResultKey) {
        HttpGet request = new HttpGet(String.format("%s/rest/api/1.0/result/%s?expand=metadata", bambooUrl, planResultKey.getKey()));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to get build result details");
            }

            String body = EntityUtils.toString(response.getEntity());

            return gson.fromJson(body, BuildResultDetails.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<Jobs.Job> getJobs(PlanKey planKey) {
        HttpGet request = new HttpGet(String.format("%s/rest/api/1.0/config/plan/%s/job", bambooUrl, planKey.getKey()));
        try (CloseableHttpResponse response = client.execute(request, authContext.get())) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new ApiException(statusCode, "Unable to get jobs");
            }

            String body = EntityUtils.toString(response.getEntity());

            return gson.fromJson(body, Jobs.class).getResults();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static class ApiException extends RuntimeException {

        public ApiException(int statusCode, String message) {
            super("Status code: " + statusCode + ". Message: " + message);
        }
    }
}
