package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageTypeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelDispatcher implements Dispatcher {
    private static final List<Subscription<?, ?>> EMPTY_LIST = new ArrayList<>();
    private final ExecutorService executorService;
    private long replyId = 0L;
    private Map<Address, List<Subscription<?, ?>>> openSubscriptions = new HashMap<>();
    private final List<Long> closedSubscriptions = new ArrayList<>();
    long idCount = 0L;

    {
        MetaRepository.INSTANCE.register(AddressMeta.getInstance());
        MetaRepository.INSTANCE.register(BridgeMessageTypeMeta.getInstance());
        MetaRepository.INSTANCE.register(PublicationMeta.getInstance());
    }

    public ParallelDispatcher(int workCount) {
        this.executorService = Executors.newFixedThreadPool(workCount, new NamedThreadFactory("ParallelDispatcher"));
    }


    @Override
    public void publish(Address to,
                        Subject publisher,
                        Publication payload,
                        BridgeMessageType publicationType) {
        publish(to, Address.NO_REPLY, publisher, payload, publicationType);
    }

    private void publish(Address to,
                 Address replyTo,
                         Subject publisher,
                         Publication publication,
                 BridgeMessageType publicationType) {
executorService.submit(() -> {

});
//        println("publishing: to=$to publication=$publication")

        List<Subscription<?, ?>> subscriptions = new ArrayList<>();
        subscriptions.addAll(openSubscriptions.getOrDefault(to, EMPTY_LIST));
        subscriptions.addAll(openSubscriptions.getOrDefault(Address.WILDCARD_ADDRESS, EMPTY_LIST));
        var openSubscriptionsIt = subscriptions.iterator();

        while (openSubscriptionsIt.hasNext()) {
            var it = openSubscriptionsIt.next();

            if (!it.isActive()) {
//                openSubscriptionsIt.remove()
//                closedSubscriptions.remove(it.getId())
                continue;
            }

            if (it.isBridged()) {
                it.onEvent(
                        to,
                        publisher,
                        ClassHelper.Companion.uncheckedCast(publication)
                );
            } else if (publication.getPayload() == Dispatcher.BRIDGE_RETURN) {

            } else if (replyTo != Address.NO_REPLY) {
                var response = it.onEvent(
                        to,
                        publisher,
                        ClassHelper.Companion.uncheckedCast(publication)
                );

                if (response != Dispatcher.BRIDGE_RETURN) {
                    Object res = (response == null) ? NullValue.INSTANCE : response;
                    publish(replyTo, publisher, new Publication(res), publicationType);
                }
            } else {
                it.onEvent(
                        to,
                        publisher,
                        ClassHelper.Companion.uncheckedCast(publication)
                );
            }
            if (!it.isActive()) {
                closedSubscriptions.remove(it.getId());
                openSubscriptionsIt.remove();
            }
        }
    }

////    override fun <Q, A> subscribe(topic: Dispatcher.Topic, handler: Consumer<A>): Dispatcher.Subscription {
////        return subscribe(topic, Function<Object, A?> { o ->
////            handler.accept(o as A)
////            null
////        })
////    }

    @Override
    public <Q, A> Subscription<Q, A> subscribe(Address to, MessageHandler<Q, A> handler) {
        return newSubscription(to, new MessageHandler<Q, A>() {
            @Override
            public A onMessage(Address to, Subject sender, Q payload) {
                return handler.onMessage(
                        to,
                        sender,
                        payload
                );
            }
        }, false);
    }

    @Override
    public <Q, A> Subscription<Q, A> bridge(Address to, MessageHandler<Q, A> handler) {
        return this.<Q, A>newSubscription(to, new MessageHandler<Q, A>() {
            @Override
            public A onMessage(Address to,
                               Subject sender,
                               Q payload) {
                return handler.onMessage(
                        to,
                        sender,
                        payload
                );
            }
        }, true);
    }

    private <Q, A> Subscription<Q, A> newSubscription(Address to, MessageHandler<Q, A> handler,
                                                      Boolean isBridged
    ) {
        class SubscriptionHolder implements Subscription<Q, A> {
            List<Subscription<?, ?>> subscriptionList = openSubscriptions.compute(to, (k, v) -> new ArrayList<>());
            private final long id = ++idCount;
//
//            init {
//                println("new subscription {id: $id to: $to isBridged: $isBridged} ")
//            }

            @Override
            public long getId() {
                return id;
            }


            {
                subscriptionList.add(this);
            }

            @Override
            public A onEvent(Address to, Subject sender, Publication publication) {
//                println("Handle event for subscription {id: $id to: $to isBridged: $isBridged} ")
                return handler.onMessage(
                        to,
                        sender,
                        (Q)publication.getPayload()
                );
            }

            @Override
            public void unsubscribe() {
//                println("Remove subscription {id: $id to: $to isBridged: $isBridged} ")
                closedSubscriptions.add(id);
            }

            @Override
            public boolean isActive() {
                return !closedSubscriptions.contains(id);
            }


            @Override
            public boolean isBridged() {
                return isBridged;
            }
        }

        return new SubscriptionHolder();
    }


    @Override
    public <Q, A> void publishAsync(Address to,
                                    Publication payload,
                                    MessageHandler<Q, A> handler) {
        AtomicReference<Subscription<?, ?>> subscriptionHolder = new AtomicReference<>();
        // Establish response subscription
        var replyTo =
                new Address("com.loudsight.utilities.service.dispatcher.ParallelDispatcher.publishAsync",
                        "" + replyId++);

        var subscription = this.subscribe(replyTo, new MessageHandler<Q, A>() {
            @Override
            public A onMessage(Address to, Subject sender, Q payload) {
                subscriptionHolder.get().unsubscribe();

                if (payload instanceof NullValue) { // This can never be true!!!!! NullValue is not an instanceof Publication
                    return handler.onMessage(to, sender, null);
                } else {
                    return handler.onMessage(to, sender, payload);
                }
            }
        });
        subscriptionHolder.set(subscription);
        publish(to, replyTo, payload.getRecipient(), payload, BridgeMessageType.DIRECT_ASYNC);
    }
}