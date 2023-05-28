package com.loudsight.utilities.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType;

public interface Dispatcher {
        String BRIDGE_RETURN = "bridgeMessage";
    

    interface Subscription<Q, A> {
        long getId();

        A onEvent(Address to,
                  Address replyTo,
                  Subject recipient,
                  Subject sender,
                  Object payload,
                  BridgeMessageType publicationType);
//               

        void unsubscribe();

        boolean isActive();

        boolean isBridged();
    }

    /**
     * Subscribe to a topic, provide a handler that accepts
     */


    <Q, A> Subscription<Q, A> subscribe(Address to, MessageHandler<A> handler);

    <T> Subscription<Publication,T> bridge(Address to, MessageHandler<T> handler);

    <T> void publishAsync(Address to,
                      Subject recipient,
                      Subject publisher,
                      Object payload,
                      MessageHandler<T> handler);
    void publish(Address to,
                 Subject recipient,
                 Subject publisher,
                 Object payload,
                 BridgeMessageType publicationType);

    default void publish(Address to, Subject recipient, Subject publisher, Object payload) {
        publish(to, recipient, publisher, payload, BridgeMessageType.DIRECT);
    }
}
