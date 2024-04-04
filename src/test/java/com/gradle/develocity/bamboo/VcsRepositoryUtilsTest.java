package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.vcs.configuration.PlanRepositoryDefinition;
import com.atlassian.bamboo.vcs.configuration.VcsLocationDefinition;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.gradle.develocity.bamboo.config.PersistentConfiguration;
import com.gradle.develocity.bamboo.config.PersistentConfigurationManager;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VcsRepositoryUtilsTest {

    private final BuildContext buildContext = mock(BuildContext.class);
    private final PersistentConfigurationManager configurationManager = mock(PersistentConfigurationManager.class);

    @ParameterizedTest
    @CsvSource({
        "+:foo, true",
        "-:foo, false",
        ",true"
    })
    void injectionIsAllowedOnVcsRepoAccordingToPattern(String pattern, boolean expected) {
        when(buildContext.getRelevantRepositoryIds()).thenReturn(Sets.newHashSet(1L));
        ImmutableMap<Long, PlanRepositoryDefinition> repoMap = ImmutableMap.of(1L, mockPlanRepo("http://foo.git", "repositoryUrl"));
        when(buildContext.getVcsRepositoryMap()).thenReturn(repoMap);
        PersistentConfiguration config = mock(PersistentConfiguration.class);
        when(config.getVcsRepositoryFilter()).thenReturn(pattern);
        when(configurationManager.load()).thenReturn(Optional.of(config));

        boolean isAllowed = VcsRepositoryUtils.injectionIsAllowedOnVcsRepo(configurationManager, buildContext);

        assertThat(isAllowed, Matchers.is(expected));
    }

    @Test
    void vcsRepoUrlsNoRepos() {
        when(buildContext.getRelevantRepositoryIds())
            .thenReturn(Collections.emptySet());

        Set<String> repoUrls = VcsRepositoryUtils.vcsRepoUrls(buildContext);

        assertThat(repoUrls, Matchers.emptyIterable());
    }

    @Test
    void vcsRepoUrls() {
        when(buildContext.getRelevantRepositoryIds())
            .thenReturn(Sets.newHashSet(1L, 2L, 3L));
        ImmutableMap<Long, PlanRepositoryDefinition> repoMap = ImmutableMap.of(
            1L, mockPlanRepo("http://foo.git", "repositoryUrl"),
            2L, mockPlanRepo("bar", "github.repository"),
            3L, mockPlanRepo("http://random", "random"));
        when(buildContext.getVcsRepositoryMap()).thenReturn(repoMap);

        Set<String> repoUrls = VcsRepositoryUtils.vcsRepoUrls(buildContext);

        assertThat(repoUrls, Matchers.equalTo(Sets.newHashSet("http://foo.git", "https://github.com/bar")));
    }

    private PlanRepositoryDefinition mockPlanRepo(String url, String key) {
        PlanRepositoryDefinition mockRepo = mock(PlanRepositoryDefinition.class);
        VcsLocationDefinition mockLoc = mock(VcsLocationDefinition.class);
        when(mockLoc.getConfiguration()).thenReturn(ImmutableMap.of(key, url));
        when(mockRepo.getVcsLocation()).thenReturn(mockLoc);
        return mockRepo;
    }


}
