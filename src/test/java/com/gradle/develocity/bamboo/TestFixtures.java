package com.gradle.develocity.bamboo;

import com.atlassian.bamboo.bandana.BambooBandanaContextImpl;
import com.atlassian.bamboo.build.BuildOutputLogEntry;
import com.atlassian.bamboo.build.DefaultBuildDefinition;
import com.atlassian.bamboo.chains.ChainStorageTag;
import com.atlassian.bamboo.credentials.CredentialsDataEntity;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.BuildContextImpl;
import com.atlassian.bamboo.v2.build.BuildIdentifierImpl;
import com.atlassian.bamboo.v2.build.BuildKey;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Collection<BuildOutputLogEntry> randomLogEntries(int size) {
        return IntStream.range(0, size)
                .mapToObj(__ ->
                        new BuildOutputLogEntry("random log - " + RandomStringUtils.randomAlphanumeric(10)))
                .collect(Collectors.toList());
    }

    public static String randomBuildScanUrl() {
        return "https://develocity-server/s/" + RandomStringUtils.randomAlphanumeric(13);
    }

    public static BuildContext getBuildContext() {
        return new BuildContextImpl(
                new BuildIdentifierImpl(null, null, "PLAN-KEY", "project", "plan", "shortname", 1),
                new ManualBuildTriggerReason(),
                new DefaultBuildDefinition(false),
                null,
                null,
                null,
                null,
                null,
                Collections.emptyMap(),
                Collections.emptySet(),
                Collections.singletonMap(RandomUtils.nextLong(), RandomStringUtils.randomAscii(10)),
                false,
                false,
                false,
                false,
                new BambooBandanaContextImpl(null, null),
                Collections.singletonList(new CredentialsDataEntity("key", "name", Collections.singletonMap("key", "value"), null, null)),
                Collections.singletonMap(PlanKeys.getPlanKey("SOME-KEY"), new ChainStorageTag("tag")),
                new BuildKey()
        );
    }

}
