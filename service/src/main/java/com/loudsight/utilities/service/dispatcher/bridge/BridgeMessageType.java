package com.loudsight.utilities.service.dispatcher.bridge;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = BridgeMessageType.class)
public enum BridgeMessageType {
    DIRECT,
    DIRECT_ASYNC,
    SUBSCRIPTION,
    PUBLICATION,
    REQUEST,
    RESPONSE,
    ERROR
}