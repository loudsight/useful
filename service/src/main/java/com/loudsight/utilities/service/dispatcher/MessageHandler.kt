package com.loudsight.utilities.service.dispatcher

import com.loudsight.utilities.permission.Subject
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType

interface MessageHandler<A> {
    fun onMessage(to: Address,
                  replyTo: Address,
                  recipient: Subject,
                  sender: Subject,
                  publicationType: BridgeMessageType,
                  payload: Any?): A
}