package com.gradle.develocity.bamboo.model;

import java.util.List;

public final class Jobs {

    private List<Job> results;

    public List<Job> getResults() {
        return results;
    }

    public static final class Job {

        private JobKey key;
        private String name;

        public JobKey getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

    }

    @Override
    public String toString() {
        return "Jobs{" +
                "results=" + results +
                '}';
    }
}
