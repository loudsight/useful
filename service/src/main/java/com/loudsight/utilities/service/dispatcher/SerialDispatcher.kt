package com.loudsight.utilities.service.dispatcher

import com.loudsight.meta.MetaRepository
import com.loudsight.utilities.permission.Subject
import com.loudsight.utilities.service.dispatcher.Address.Companion.NO_REPLY
import com.loudsight.utilities.service.dispatcher.Dispatcher.Subscription
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageType
import com.loudsight.utilities.service.dispatcher.bridge.BridgeMessageTypeMeta

open class SerialDispatcher : Dispatcher {
    private var replyId = 0
    val openSubscriptions = HashMap<Address, MutableList<Subscription<*, *>>>()
    val closedSubscriptions = ArrayList<Long>()
    var idCount = 0L;

    init {
        MetaRepository.register(AddressMeta)
        MetaRepository.register(BridgeMessageTypeMeta)
        MetaRepository.register(PublicationMeta)

    }

    override fun publish(
        to: Address,
        recipient: Subject,
        publisher: Subject,
        payload: Any?,
        publicationType: BridgeMessageType
    ) {
        publish(to, NO_REPLY, recipient, publisher, payload, publicationType)
    }


    private fun publish(
        to: Address,
        replyTo: Address,
        recipient: Subject,
        publisher: Subject,
        payload: Any?,
        publicationType: BridgeMessageType
    ) {

        println("publishing: to=$to payload=$payload")

        val subscriptions = ArrayList<Subscription<Any, Any>>()
        subscriptions.addAll(
            (openSubscriptions.getOrElse(to) { EMPTY_LIST } as MutableList<Subscription<Any, Any>>)
        )
        subscriptions.addAll(
            (openSubscriptions.getOrElse(WildCardAddress) { EMPTY_LIST } as MutableList<Subscription<Any, Any>>)
        )
        val openSubscriptionsIt = subscriptions.iterator()

        while (openSubscriptionsIt.hasNext()) {
            val it = openSubscriptionsIt.next()

            if (!it.isActive()) {
//                openSubscriptionsIt.remove()
//                closedSubscriptions.remove(it.getId())
                continue
            }

            if (it.isBridged()) {
                it.onEvent(
                    to,
                    replyTo,
                    recipient,
                    publisher,
                    payload,
                    publicationType
                )
            } else if (payload == Dispatcher.BRIDGE_RETURN) {

            } else if (replyTo != NO_REPLY) {
                val response = it.onEvent(
                    to,
                    replyTo,
                    recipient,
                    publisher,
                    payload,
                    publicationType
                )

                if (response != Dispatcher.BRIDGE_RETURN) {
                    val res = response ?: NullValue()
                    publish(replyTo, recipient, publisher, res, publicationType)
                }
            } else {
                it.onEvent(
                    to,
                    replyTo,
                    recipient,
                    publisher,
                    payload,
                    publicationType
                )
            }
            if (!it.isActive()) {
                closedSubscriptions.remove(it.getId())
                openSubscriptionsIt.remove()
            }
        }
    }

//    override fun <Q, A> subscribe(topic: Dispatcher.Topic, handler: Consumer<A>): Dispatcher.Subscription {
//        return subscribe(topic, Function<Any, A?> { o ->
//            handler.accept(o as A)
//            null
//        })
//    }

    override fun <Q, A> subscribe(to: Address, handler: MessageHandler<A>): Subscription<Q, A> {
        return newSubscription(to, object : MessageHandler<A> {
            override fun onMessage(
                to: Address,
                replyTo: Address,
                recipient: Subject,
                sender: Subject,
                publicationType: BridgeMessageType,
                payload: Any?
            ): A {
                return handler.onMessage(
                    to,
                    replyTo,
                    recipient,
                    sender,
                    publicationType,
                    payload
                )
            }
        }, false)
    }

    override fun bridge(to: Address, handler: MessageHandler<Unit>): Subscription<Publication, Unit> {
        return newSubscription(to, object : MessageHandler<Unit> {

            override fun onMessage(
                to: Address,
                replyTo: Address,
                recipient: Subject,
                sender: Subject,
                publicationType: BridgeMessageType,
                payload: Any?,
            ) {
                return handler.onMessage(
                    to,
                    replyTo,
                    recipient,
                    sender,
                    publicationType,
                    payload
                )
            }
        }, true)
    }

    private fun <Q, A> newSubscription(
        to: Address,
        handler: MessageHandler<A>,
        isBridged: Boolean
    ): Subscription<Q, A> {
        class SubscriptionHolder : Subscription<Q, A> {
            val subscriptionList = openSubscriptions.getOrPut(to) { ArrayList() }
            private val id = ++idCount

            init {
                println("new subscription {id: $id to: $to isBridged: $isBridged} ")
            }

            override fun getId(): Long {
                return id
            }

            init {
                subscriptionList.add(this)
            }

            override fun onEvent(
                to: Address,
                replyTo: Address,
                recipient: Subject,
                sender: Subject,
                payload: Any?,
                publicationType: BridgeMessageType
            ): A {
                println("Handle event for subscription {id: $id to: $to isBridged: $isBridged} ")
                return handler.onMessage(
                    to,
                    replyTo,
                    recipient,
                    sender,
                    publicationType,
                    payload,
                )
            }

            override fun unsubscribe() {
                println("Remove subscription {id: $id to: $to isBridged: $isBridged} ")
                closedSubscriptions.add(id)
            }

            override fun isActive(): Boolean {
                return !closedSubscriptions.contains(id)
            }

            override fun isBridged(): Boolean {
                return isBridged
            }
        }

        return SubscriptionHolder()
    }

    override fun publishAsync(
        to: Address,
        recipient: Subject,
        publisher: Subject,
        payload: Any?,
        handler: MessageHandler<Unit>
    ) {
        var subscriptionHolder: Subscription<*, *>? = null
        // Establish response subscription
        val replyTo =
            Address("com.loudsight.utilities.service.dispatcher.SerialDispatcher.publishAsync", "${replyId++}")

        val subscription = this.subscribe<Any, Unit>(replyTo, object : MessageHandler<Unit> {
            override fun onMessage(
                to: Address,
                replyTo: Address,
                recipient: Subject,
                sender: Subject,
                publicationType: BridgeMessageType,
                payload: Any?
            ) {
                subscriptionHolder?.unsubscribe()
                if (payload is NullValue) {
                    handler.onMessage(to, replyTo, recipient, sender, publicationType, null)
                } else {
                    handler.onMessage(to, replyTo, recipient, sender, publicationType, payload)
                }
            }
        })
        subscriptionHolder = subscription
        publish(to, replyTo, publisher, recipient, payload, BridgeMessageType.DIRECT_ASYNC)
    }

    private companion object {
        private val EMPTY_LIST = ArrayList<Subscription<Any, Any>>()
    }
}