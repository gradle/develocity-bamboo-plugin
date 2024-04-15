package it.com.gradle.develocity.bamboo;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ratpack.handling.Context;
import ratpack.http.Response;
import ratpack.http.Status;
import ratpack.test.embed.EmbeddedApp;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class MockDevelocityServer implements BeforeEachCallback, AfterEachCallback {

    private static final long TEN_MEGABYTES_IN_BYTES = 1024 * 1024 * 10;

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper(new JsonFactory());
    private static final ObjectWriter JSON_WRITER = JSON_OBJECT_MAPPER.writer();

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {
            };

    private static final String PUBLIC_BUILD_SCAN_ID = "z7o6hj5ag6bpc";
    private static final String DEFAULT_SCAN_UPLOAD_TOKEN = "scan-upload-token";

    private final List<ScanTokenRequest> scanTokenRequests = Collections.synchronizedList(new LinkedList<>());

    private boolean rejectUpload;
    private EmbeddedApp mockDevelocityServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        mockDevelocityServer = EmbeddedApp.fromHandlers(
                c -> c
                        .prefix("scans/publish", c1 -> c1
                                .post("gradle/:pluginVersion/token", this::handleToken)
                                .post("gradle/:pluginVersion/upload", this::handleUpload)
                                .notFound()
                        )
        );
    }

    @Override
    public void afterEach(ExtensionContext context) {
        mockDevelocityServer.close();
    }

    private void handleToken(Context ctx) {
        ctx.getRequest().getBody(TEN_MEGABYTES_IN_BYTES).then(request -> {
            Map<String, Object> requestBody =
                    JSON_OBJECT_MAPPER.readValue(request.getText(), MAP_TYPE_REFERENCE);

            scanTokenRequests.add(
                    new ScanTokenRequest(
                            (String) requestBody.get("buildToolType"),
                            (String) requestBody.get("buildToolVersion"),
                            (String) requestBody.get("buildAgentVersion")
                    ));

            Map<String, String> responseBody =
                    ImmutableMap.of(
                            "id", PUBLIC_BUILD_SCAN_ID,
                            "scanUrl", publicBuildScanId(),
                            "scanUploadUrl", scanUploadUrl(ctx),
                            "scanUploadToken", DEFAULT_SCAN_UPLOAD_TOKEN
                    );

            ctx.getResponse()
                    .contentType("application/vnd.gradle.scan-ack+json")
                    .send(JSON_WRITER.writeValueAsBytes(responseBody));
        });
    }

    private void handleUpload(Context ctx) {
        ctx.getRequest().getBody(TEN_MEGABYTES_IN_BYTES)
                .then(__ -> {
                    Response response = ctx.getResponse();
                    if (rejectUpload) {
                        response
                                .status(Status.BAD_GATEWAY)
                                .send();
                    } else {
                        response
                                .contentType("application/vnd.gradle.scan-upload-ack+json")
                                .send("{}");
                    }
                });
    }

    private String scanUploadUrl(Context ctx) {
        String pluginVersion = ctx.getPathTokens().get("pluginVersion");
        return String.format("%sscans/publish/gradle/%s/upload", getAddress(), pluginVersion);
    }

    public String publicBuildScanId() {
        return String.format("%ss/%s", getAddress(), PUBLIC_BUILD_SCAN_ID);
    }

    public URI getAddress() {
        Preconditions.checkNotNull(mockDevelocityServer, "mockDevelocityServer has not yet been created");
        return mockDevelocityServer.getAddress();
    }

    @Nullable
    public ScanTokenRequest getLastScanTokenRequest() {
        return Iterables.getLast(scanTokenRequests, null);
    }

    public void rejectUpload() {
        this.rejectUpload = true;
    }

    public static final class ScanTokenRequest {

        public final String toolType;
        public final String toolVersion;
        public final String agentVersion;

        private ScanTokenRequest(String toolType, String toolVersion, String agentVersion) {
            this.toolType = Preconditions.checkNotNull(toolType, "toolType must not be null");
            this.toolVersion = Preconditions.checkNotNull(toolVersion, "toolVersion must not be null");
            this.agentVersion = Preconditions.checkNotNull(agentVersion, "agentVersion must not be null");
        }
    }
}
