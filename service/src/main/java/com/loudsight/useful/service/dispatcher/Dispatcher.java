package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;

public interface Dispatcher {
    String BRIDGE_RETURN = "bridgeMessage";

    interface Subscription<Q, A> {
        long getId();

        A onEvent(Address to,
                  Subject sender,
                  Publication payload);
//               

        void unsubscribe();

        boolean isActive();

        boolean isBridged();
    }

    /**
     * Subscribe to a topic, provide a handler that accepts
     */


    <Q, A> Subscription<Q, A> subscribe(Address to, MessageHandler<Q, A> handler);

    <Q, A> Subscription<Q, A> bridge(Address to, MessageHandler<Q, A> handler);

    <Q, A> void publishAsync(Address to, Publication payload, MessageHandler<Q, A> handler);
    void publish(Address to,
                 Subject publisher,
                 Publication payload,
                 BridgeMessageType publicationType);

    default void publish(Address to, Subject publisher, Publication payload) {
        publish(to, publisher, payload, BridgeMessageType.DIRECT);
    }
}
