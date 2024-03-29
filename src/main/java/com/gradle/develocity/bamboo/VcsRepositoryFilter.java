package com.gradle.develocity.bamboo;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class VcsRepositoryFilter {

    public static final VcsRepositoryFilter EMPTY = new VcsRepositoryFilter("", Collections.emptyList(), Collections.emptyList());

    public static final String INCLUSION_QUALIFIER = "+:";
    public static final String EXCLUSION_QUALIFIER = "-:";

    public static final String SEPARATOR = "\n";

    private final String vcsRepositoryFilter;
    private final List<String> inclusion;
    private final List<String> exclusion;

    private VcsRepositoryFilter(String vcsRepositoryFilter, List<String> inclusion, List<String> exclusion) {
        this.vcsRepositoryFilter = vcsRepositoryFilter;
        this.inclusion = inclusion;
        this.exclusion = exclusion;
    }

    public static VcsRepositoryFilter of(String filter) {
        if (StringUtils.isBlank(filter)) {
            return EMPTY;
        }

        List<String> inclusionFilters = new ArrayList<>();
        List<String> exclusionFilters = new ArrayList<>();

        Arrays.stream(filter.split(SEPARATOR))
            .map(String::trim)
            .filter(s -> !StringUtils.isBlank(s))
            .forEach(pattern -> {
                if (pattern.startsWith(INCLUSION_QUALIFIER)) {
                    String candidate = pattern.substring(INCLUSION_QUALIFIER.length()).trim();
                    if (!StringUtils.isBlank(candidate)) {
                        inclusionFilters.add(candidate);
                    }
                } else if (pattern.startsWith(EXCLUSION_QUALIFIER)) {
                    String candidate = pattern.substring(EXCLUSION_QUALIFIER.length()).trim();
                    if (!StringUtils.isBlank(candidate)) {
                        exclusionFilters.add(candidate);
                    }
                }
            });

        return new VcsRepositoryFilter(
            filter,
            ImmutableList.copyOf(inclusionFilters),
            ImmutableList.copyOf(exclusionFilters)
        );
    }

    public boolean isEmpty() {
        return inclusion.isEmpty() && exclusion.isEmpty();
    }

    enum Result {
        INCLUDED, EXCLUDED, NOT_MATCHED
    }

    public Result matches(String url) {
        if (matchesRepositoryFilter(url, exclusion)) {
            return Result.EXCLUDED;
        }
        if (matchesRepositoryFilter(url, inclusion)) {
            return Result.INCLUDED;
        }
        return Result.NOT_MATCHED;
    }

    private boolean matchesRepositoryFilter(String repositoryUrl, Collection<String> patterns) {
        if (StringUtils.isBlank(repositoryUrl)) {
            return false;
        }
        for (String pattern : patterns) {
            if (repositoryUrl.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    String getVcsRepositoryFilter() {
        return vcsRepositoryFilter;
    }
}
