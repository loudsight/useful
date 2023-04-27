package com.loudsight.utilities.service.dispatcher.bridge

import com.loudsight.meta.MetaRepository
import com.loudsight.utilities.permission.Subject
import com.loudsight.utilities.service.dispatcher.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BridgeTransport(val name: String,
                               val dispatcher: Dispatcher,
                               val pull: (onResponse: (Any) -> Unit) -> Unit,
                               override val coroutineContext: CoroutineContext) : CoroutineScope {

    constructor(
        name: String,
        dispatcher: Dispatcher,
        coroutineContext: CoroutineContext
    ) :
            this(
                name,
                dispatcher,
                NoOpHandler,
                coroutineContext
            )

    val outgoingMessageListeners: MutableList<(Publication) -> Unit> = mutableListOf()

    init {
        MetaRepository.register(AddressMeta)
        MetaRepository.register(BridgeMessageTypeMeta)

        launch {
            while (true) {
                pull.invoke { processIncoming(it) }
                delay(1000)
            }
        }
    }

    fun addOutgoingMessageListener(listener: (Publication) -> Unit) {
        outgoingMessageListeners.add(listener)
    }

    fun give(message: Publication) {
        outgoingMessageListeners.forEach {
            it.invoke(message)
        }
    }

    fun take(message: Publication) {
        processIncoming(message)
    }

    fun push(to: Address) {
        val serviceName = to.scope
        val topicName = to.topic
        dispatcher.bridge(Address(serviceName, topicName), object : MessageHandler<Unit> {
            override fun onMessage(
                to: Address,
                replyTo: Address,
                recipient: Subject,
                sender: Subject,
                publicationType: BridgeMessageType, payload: Any?
            ) {
                give(Publication(to, replyTo, recipient, sender, payload, publicationType))
            }
        })
    }

    fun processIncoming(message: Any) {
        if (message is Publication) {
            processBridgeMessage(message)
        } else if (message is Collection<*>) {
            message.forEach {
                processIncoming(it!!)
            }
        }
    }

    private fun processBridgeMessage(publication: Publication) {

        when (publication.publicationType) {
            BridgeMessageType.DIRECT, BridgeMessageType.PUBLICATION -> {
                dispatcher.publish(publication.to, publication.recipient, publication.sender, publication.payload, publication.publicationType)
            }

            BridgeMessageType.DIRECT_ASYNC, BridgeMessageType.REQUEST -> {
                dispatcher.publishAsync(
                    publication.to, publication.sender, publication.sender, publication.payload
                , object: MessageHandler<Unit> {
                        override fun onMessage(
                            to: Address,
                            replyTo: Address,
                            recipient: Subject,
                            sender: Subject,
                            publicationType: BridgeMessageType,
                            payload: Any?
                        ) {
                            val bridgedPublication = Publication(
                                to,
                                replyTo,
                                recipient,
                                sender,
                                payload,
                                BridgeMessageType.RESPONSE
                            )
                            give(bridgedPublication)
                        }
                })
            }

            BridgeMessageType.RESPONSE -> {
                dispatcher.publish(publication.to, publication.recipient, publication.sender, publication.payload, publication.publicationType)
            }

//            BridgeMessageType.SUBSCRIPTION -> {
//                    dispatcher.subscribe<Any, Any>(Address(serviceName, topicName)) {
//                        transport.send(BridgeMessage(BridgeMessageType.PUBLICATION, subscriptionRequest, it))
//                    }
//            }
            else -> {}
        }
    }

    companion object {
        val NoOpHandler: (onResponse: (ByteArray) -> Unit) -> Unit = {}
    }
}
