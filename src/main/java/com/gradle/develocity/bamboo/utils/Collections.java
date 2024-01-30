package com.gradle.develocity.bamboo.utils;

import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class Collections {

    private static final Comparator<Ordered> BY_ORDER = Comparator.comparing(Ordered::getOrder);

    private Collections() {
    }

    public static <T> List<T> sorted(Collection<T> collection, Comparator<? super T> comparator) {
        return collection.stream().sorted(comparator).collect(Collectors.toList());
    }

    public static <T extends Ordered> List<T> sortedByOrder(Collection<T> collection) {
        return sorted(collection, BY_ORDER);
    }
}
