package com.loudsight.utilities.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType;

public interface MessageHandler<A> {
    A onMessage(Address to,
                       Address replyTo,
                       Subject recipient,
                       Subject sender,
                       BridgeMessageType publicationType,
                       Object payload);
}