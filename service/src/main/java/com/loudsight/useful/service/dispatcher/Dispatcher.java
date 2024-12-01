package com.loudsight.useful.service.dispatcher;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Dispatcher {

    <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, Topic<P, A, ?> responseTopic, Function<Q, A> handler);

    default <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, Function<Q, A> handler) {
        return subscribe(requestTopic, null, handler);
    }

    <P, Q, A> void publish(Topic<P, Q, A> requestTopic, Topic<?, A, ?> responseTopic, Q payload);

    default <P, Q, A> void publish(Topic<P, Q, A> requestTopic, Q payload) {
        publish(requestTopic, null, payload);
    }

    <P, Q, A> void publishAsync(Topic<P, Q, A> topic, Q payload, Consumer<A> handler);
}
