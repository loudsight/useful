package com.loudsight.utilities.service.dispatcher

import com.loudsight.meta.MetaRepository
import com.loudsight.utilities.permission.Subject
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageTypeMeta

interface Dispatcher {
    companion object {
        const val BRIDGE_RETURN = "bridgeMessage"
    }

    interface Subscription<Q, A> {
        fun getId(): Long

        fun onEvent(
            to: Address,
            replyTo: Address,
            recipient: Subject,
            sender: Subject,
            payload: Any?,
            publicationType: BridgeMessageType
        ): A?

        fun unsubscribe()

        fun isActive(): Boolean

        fun isBridged(): Boolean
    }

    /**
     * Subscribe to a topic, provide a handler that accepts
     */
    fun <Q, A> subscribe(to: Address, handler: MessageHandler<A>): Subscription<Q, A>

    fun bridge(to: Address, handler: MessageHandler<Unit>): Subscription<Publication, Unit>

    fun publishAsync(to: Address, recipient: Subject, publisher: Subject, payload: Any?, handler: MessageHandler<Unit>)

    fun publish(to: Address, recipient: Subject, publisher: Subject, payload: Any?, publicationType: BridgeMessageType)

    fun publish(to: Address, recipient: Subject, publisher: Subject, payload: Any?) {
        publish(to, recipient, publisher, payload, BridgeMessageType.DIRECT)
    }
}
