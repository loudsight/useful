package com.loudsight.useful.service.dispatcher;

public record Topic<Q, A>(Address to, Class<Q> requestType, Class<A> responseType) {
    public static Topic<?, ?> NO_REPLY = new Topic<>("no-reply", "no-reply", Void.class, Void.class);
    public static Topic<Object, Object> WILDCARD_ADDRESS = new Topic<>("*", "*", Object.class, Object.class);

    public Topic(String topicScope, String topicName, Class<Q> requestType, Class<A> responseType) {
        this(new Address(topicScope, topicName), requestType, responseType);
    }
}
