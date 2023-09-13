package com.gradle.enterprise.bamboo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class JsonConfigurationConverterTest {

    private static final PersistentConfiguration config = new PersistentConfiguration()
        .setServer("https://mycompany.com")
        .setGePluginVersion("3.11")
        .setPluginRepository("https://plugins.mycompany.com")
        .setSharedCredentialName("ge-creds")
        .setAllowUntrustedServer(true)
        .setCcudPluginVersion("1.11")
        .setInjectCcudExtension(true)
        .setInjectMavenExtension(true);

    private static final String json = "{\"server\":\"https://mycompany.com\",\"allowUntrustedServer\":true," +
        "\"sharedCredentialName\":\"ge-creds\",\"gePluginVersion\":\"3.11\",\"ccudPluginVersion\":\"1.11\"," +
        "\"pluginRepository\":\"https://plugins.mycompany.com\",\"injectMavenExtension\":true," +
        "\"injectCcudExtension\":true}";

    @Test
    void toJson() throws JsonProcessingException {
        assertThat(new JsonConfigurationConverter().toJson(config), is(json));
    }

    @Test
    void toNullJson() throws JsonProcessingException {
        assertThat(new JsonConfigurationConverter().toJson(null), nullValue());
    }

    @Test
    void fromJson() throws IOException {
        assertThat(new JsonConfigurationConverter().fromJson(json), equalTo(config));
    }

    @Test
    void fromNullJson() throws IOException {
        assertThat(new JsonConfigurationConverter().fromJson(null), nullValue());
    }

    @Test
    void unknownAttributesAreIgnored() throws IOException {
        String jsonConfig = "{\"server\":\"https://mycompany.com\",\"foo\":\"bar\"}";

        assertThat(new JsonConfigurationConverter().fromJson(jsonConfig),
            equalTo(new PersistentConfiguration().setServer("https://mycompany.com")));
    }
}
