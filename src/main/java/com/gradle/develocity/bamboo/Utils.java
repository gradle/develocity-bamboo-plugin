package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.v2.build.BuildContext;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class Utils {

    private Utils() {
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
