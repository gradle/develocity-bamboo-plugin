package com.gradle.develocity.bamboo.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

public final class Agent {

    private long id;
    private String name;
    private Type type;
    private boolean active;
    private boolean busy;
    private boolean enabled;

    public Agent(long id, String name, Type type, boolean active, boolean busy, boolean enabled) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.active = active;
        this.busy = busy;
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRemote() {
        return type == Type.REMOTE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .append("type", type)
            .append("active", active)
            .append("busy", busy)
            .append("enabled", enabled)
            .toString();
    }

    public enum Type {

        LOCAL, REMOTE
    }
}
