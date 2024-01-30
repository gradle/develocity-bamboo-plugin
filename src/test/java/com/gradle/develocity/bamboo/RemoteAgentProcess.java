package com.gradle.develocity.bamboo;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Uninterruptibles;
import com.gradle.develocity.bamboo.model.Agent;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.Validate;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;
import org.zeroturnaround.process.Java8Process;
import org.zeroturnaround.process.ProcessUtil;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class RemoteAgentProcess implements AutoCloseable {

    private static final String JAVA = String.join(File.separator, SystemUtils.JAVA_HOME, "bin", "java");

    private final String bambooUrl;
    private final BambooApi bambooApi;

    private RemoteAgent remoteAgent;

    public RemoteAgentProcess(String bambooUrl, BambooApi bambooApi) {
        this.bambooUrl = bambooUrl;
        this.bambooApi = bambooApi;
    }

    public synchronized void startAgentAndWaitForReadiness() {
        Validate.validState(remoteAgent == null, "Build remoteAgent is already running");

        Set<Long> existingRemoteAgents =
            bambooApi.getAgents()
                .stream()
                .filter(Agent::isRemote)
                .map(Agent::getId)
                .collect(Collectors.toSet());

        File agentHome = createTempDirectory("bambooAgentHome");
        File agentJar = bambooApi.downloadAgentJar();

        Process process = startAgent(agentHome, agentJar);

        Set<Long> remoteAgents =
            bambooApi.getAgents()
                .stream()
                .filter(Agent::isRemote)
                .map(Agent::getId)
                .filter(i -> !existingRemoteAgents.contains(i))
                .collect(Collectors.toSet());

        Validate.validState(
            remoteAgents.size() == 1, "Unexpected number of running agents. Expected 1, but got %d", remoteAgents.size());

        this.remoteAgent = new RemoteAgent(process, Iterables.getOnlyElement(remoteAgents));
    }

    private Process startAgent(File agentHome, File agentJar) {
        try {
            CountDownLatch latch = new CountDownLatch(1);

            StartedProcess buildAgentProcess = new ProcessExecutor()
                .command(JAVA, "-Dbamboo.home=" + agentHome.getAbsolutePath(), "-DdisableBootstrapUpdate", "-jar", agentJar.getAbsolutePath(), bambooUrl + "/agentServer/")
                .environment("DISABLE_AGENT_AUTO_CAPABILITY_DETECTION", "true")
                .redirectOutput(new LogOutputStream() {

                    private final Pattern buildAgentStartedMessage = Pattern.compile(".*Bamboo agent '.+' ready to receive builds\\..*");

                    @Override
                    protected void processLine(String line) {
                        if (buildAgentStartedMessage.matcher(line).matches()) {
                            latch.countDown();
                        }
                        System.out.println(line);
                    }
                })
                .redirectError(new LogOutputStream() {

                    @Override
                    protected void processLine(String line) {
                        System.err.println(line);
                    }
                })
                .start();

            boolean started = Uninterruptibles.awaitUninterruptibly(latch, 1, TimeUnit.MINUTES);
            if (!started) {
                throw new RuntimeException("Build agent failed to start");
            }

            return buildAgentProcess.getProcess();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public synchronized void close() throws Exception {
        if (remoteAgent != null) {
            ProcessUtil.destroyGracefullyOrForcefullyAndWait(
                new Java8Process(remoteAgent.process),
                30, TimeUnit.SECONDS,
                10, TimeUnit.SECONDS
            );
            if (bambooApi.deleteAgentSupported()) {
                bambooApi.deleteAgent(remoteAgent.id);
            }
        }
    }

    private static File createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class RemoteAgent {

        private final Process process;
        private final long id;

        RemoteAgent(Process process, long id) {
            this.process = process;
            this.id = id;
        }
    }
}
