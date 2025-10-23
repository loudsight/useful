package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

import java.util.Collections;
import java.util.Map;

@Introspect(clazz = Topic.class)
public record Topic<P, I, O/* extends Response*/>(
        Class<P> publisher,
        Class<I> requestType,
        Class<O> responseType,
        Map<String, Object> properties) {

    public static Topic<?, ?, ?> NO_REPLY = new Topic<>(Object.class, Object.class, Object.class, Collections.emptyMap());
    public static Topic<Object, Object, Object> WILDCARD_ADDRESS =
            new Topic<>(Object.class, Object.class, Object.class);

    public Topic(Class<P> publisherClass, Class<I> requestType, Class<O> responseType) {
        this(
                publisherClass,
                requestType,
                responseType,
                Collections.emptyMap());
    }

//    public static Map<Object, Object> zip(Object... elements) {
//        var map = new HashMap<>();
//        for (int i = 0; i < elements.length/2; i+=2) {
//            map.put(elements[i], elements[i + 1]);
//        }
//        return map;
//    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public <T> T getPropertyOrDefault(String key, Object defaultValue) {
        return (T)properties.getOrDefault(key, defaultValue);
    }
}
