package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;

import java.util.function.Consumer;

public interface Dispatcher {
    String BRIDGE_RETURN = "bridgeMessage";

    <P, Q, A> Subscription<P, Q, A> bridge(Topic<P, Q, A> topic, MessageHandler<Q, A> handler);

    <P, Q, A> void publishAsync(Topic<P, Q, A> topic, Q payload, Consumer<A> handler);

    <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> topic, MessageHandler<Q, A> handler);

    <P, Q, A> void publish(Topic<P, Q, A> topic, Subject publisher, Q payload);

//    default <P, Q, A> void publish(Topic<P, Q, A> topic, Subject publisher, Publication payload) {
//        publish(to, publisher, payload, BridgeMessageType.DIRECT);
//    }
}
