package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.v2.build.BuildContext;
import com.gradle.develocity.bamboo.config.GradleConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class VcsRepositoryUtils {

    private VcsRepositoryUtils() {
    }

    static boolean injectionIsAllowedOnVcsRepo(PersistentConfigurationManager configurationManager, BuildContext buildContext) {
        // Loading a GradleConfiguration is not needed, we can refactor this later to load some general settings instead
        return configurationManager.load().map(GradleConfiguration::of)
            .map(c -> c.vcsRepositoryFilter)
            .filter(f -> StringUtils.isNotBlank(f.getVcsRepositoryFilter()))
            .map(f -> {
                for (String url : vcsRepoUrls(buildContext)) {
                    switch (f.matches(url)) {
                        case EXCLUDED:
                            return false;
                        case INCLUDED:
                            return true;
                    }
                }
                return false;
            }).orElse(true);
    }

    static Set<String> vcsRepoUrls(BuildContext buildContext) {
        return buildContext.getRelevantRepositoryIds().stream().map(id -> buildContext.getVcsRepositoryMap().get(id))
            .map(r -> r.getVcsLocation().getConfiguration())
            .flatMap(c -> c.keySet().stream().map(k -> {
                if (k.contains("repositoryUrl")) {
                    return c.get(k);
                } else if (k.contains("github.repository")) {
                    return "https://github.com/" + c.get(k);
                } else {
                    return null;
                }
            }))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
