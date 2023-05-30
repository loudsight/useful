package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;

public interface MessageHandler<Q, A> {
    A onMessage(Address to,
                       Address replyTo,
                       Subject recipient,
                       Subject sender,
                       BridgeMessageType publicationType,
                       Q payload);
}