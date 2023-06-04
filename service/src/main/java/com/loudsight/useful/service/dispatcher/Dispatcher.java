package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;

import java.util.function.Consumer;

public interface Dispatcher {
    String BRIDGE_RETURN = "bridgeMessage";

    <Q, A> Subscription<Q, A> bridge(Topic<Q, A> topic, MessageHandler<Q, A> handler);

    <Q, A> void publishAsync(Topic<Q, A> topic, Q payload, Consumer<A> handler);

    <Q, A> Subscription<Q, A> subscribe(Topic<Q, A> topic, MessageHandler<Q, A> handler);

    <Q, A> void publish(Topic<Q, A> topic, Subject publisher, Q payload);

//    default <Q, A> void publish(Topic<Q, A> topic, Subject publisher, Publication payload) {
//        publish(to, publisher, payload, BridgeMessageType.DIRECT);
//    }
}
