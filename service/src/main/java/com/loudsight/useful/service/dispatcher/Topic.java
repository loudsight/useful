package com.loudsight.useful.service.dispatcher;

import java.util.HashMap;
import java.util.Map;

public record Topic<P, I, O>(
        Class<P> publisher,
        Class<I> requestType,
        Class<O> responseType,
        Map<Object, Object> properties) {

    public static Topic<?, ?, ?> NO_REPLY = new Topic<>(Object.class, Void.class, Void.class);
    public static Topic<Object, Object, Object> WILDCARD_ADDRESS =
            new Topic<>(Object.class, Object.class, Object.class);

    public Topic(Class<P> publisherClass, Class<I> requestType, Class<O> responseType, Object... properties) {
        this(
                publisherClass,
                requestType,
                responseType,
                zip(properties));
    }

    public static Map<Object, Object> zip(Object... elements) {
        var map = new HashMap<>();
        for (int i = 0; i < elements.length/2; i+=2) {
            map.put(elements[i], elements[i + 1]);
        }
        return map;
    }

    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getPropertyOrDefault(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }
}
