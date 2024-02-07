package com.gradle.develocity.bamboo.model;

import java.util.List;

public final class BuildResultDetails {

    private String lifeCycleState;
    private Metadata metadata;

    public Metadata getMetadata() {
        return metadata;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public static final class Metadata {

        private List<Item> item;

        public List<Item> getItems() {
            return item;
        }

        public static final class Item {

            private String key;
            private String value;

            public String getKey() {
                return key;
            }

            public String getValue() {
                return value;
            }

            @Override
            public String toString() {
                return "Item{" +
                        "key='" + key + '\'' +
                        ", value='" + value + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "Metadata{" +
                    "item=" + item +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "BuildResultDetails{" +
                "lifeCycleState='" + lifeCycleState + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
