package com.loudsight.useful.service.dispatcher;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface Dispatcher extends AutoCloseable {

    @Override
    void close();

    <P, Q, A> SubscriptionHandle<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, @Nullable Topic<P, A, ?> responseTopic, Function<Q, @Nullable A> handler);

    default <P, Q, A> SubscriptionHandle<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, Function<Q, @Nullable A> handler) {
        return subscribe(requestTopic, null, handler);
    }

    <P, Q, A> void publish(Topic<P, Q, A> requestTopic, @Nullable Topic<?, A, ?> responseTopic, Q payload);

    default <P, Q, A> void publish(Topic<P, Q, A> requestTopic, Q payload) {
        publish(requestTopic, null, payload);
    }

    <P, Q, A> void publishAsync(Topic<P, Q, A> topic, Q payload, Consumer<@Nullable A> handler);
}
