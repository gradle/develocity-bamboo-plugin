package com.gradle.develocity.bamboo;

import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import org.springframework.stereotype.Component;

/**
 * Factory is needed to create a new instance based on `allowUntrusted` which can be changed at runtime.
 */
@Component
public class ShortLivedTokenClientFactory {

    public ShortLivedTokenClient create(PersistentConfiguration configuration) {
        return new ShortLivedTokenClient(configuration);
    }

}
