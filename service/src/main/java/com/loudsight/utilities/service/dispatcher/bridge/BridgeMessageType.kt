package com.loudsight.utilities.service.dispatcher.bridge

import com.loudsight.meta.annotation.Introspect

@Introspect(BridgeMessageType::class)
enum class BridgeMessageType {
    DIRECT,
    DIRECT_ASYNC,
    SUBSCRIPTION,
    PUBLICATION,
    REQUEST,
    RESPONSE,
    ERROR
}