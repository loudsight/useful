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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ParallelDispatcher implements Dispatcher {
    private static final List<Subscription<?, ?>> EMPTY_LIST = new ArrayList<>();
    private final ExecutorService executorService;
    private long replyId = 0L;
    private final Map<Topic, List<Subscription<?, ?>>> openSubscriptions = new HashMap<>();
    private final List<Long> closedSubscriptions = new ArrayList<>();
    private final AtomicLong idCount = new AtomicLong();

    static {
        MetaRepository.INSTANCE.register(AddressMeta.getInstance());
        MetaRepository.INSTANCE.register(BridgeMessageTypeMeta.getInstance());
        MetaRepository.INSTANCE.register(PublicationMeta.getInstance());
    }

    public ParallelDispatcher(int workCount) {
        this.executorService = Executors.newFixedThreadPool(workCount, new NamedThreadFactory("ParallelDispatcher"));
    }
    @Override
    public  <Q, A> void publish(Topic<Q, A> topic,
                                  Subject publisher,
                                  Q payload) {
        publish(topic, Topic.NO_REPLY, publisher, payload, BridgeMessageType.DIRECT);
    }

    protected <Q, A> void publish(Topic<Q, A> topic,
                        Subject publisher,
                        Q payload,
                        BridgeMessageType publicationType) {
        publish(topic, Topic.NO_REPLY, publisher, payload, publicationType);
    }

    private <Q, A>  void publish(Topic<?, ?> topic,
                                 Topic<?, ?> replyTo,
                         Subject publisher,
                         Q publication,
                 BridgeMessageType publicationType) {
executorService.submit(() -> {

});
//        println("publishing: topic=$topic publication=$publication")

        List<Subscription<?, ?>> subscriptions = new ArrayList<>();
        subscriptions.addAll(openSubscriptions.getOrDefault(topic, EMPTY_LIST));
        subscriptions.addAll(openSubscriptions.getOrDefault(Topic.WILDCARD_ADDRESS, EMPTY_LIST));
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
                        publisher,
                        ClassHelper.Companion.uncheckedCast(publication)
                );
            } else if (publication == Dispatcher.BRIDGE_RETURN) {

            } else if (replyTo != Topic.NO_REPLY) {
                var response = it.onEvent(
                        publisher,
                        ClassHelper.Companion.uncheckedCast(publication)
                );

                if (response != Dispatcher.BRIDGE_RETURN) {
                    Object res = (response == null) ? NullValue.INSTANCE : response;
                    publish((Topic)replyTo, publisher, res, publicationType);
                }
            } else {
                it.onEvent(
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
    public <Q, A> Subscription<Q, A> subscribe(Topic<Q, A> topic, MessageHandler<Q, A> handler) {
        return newSubscription(topic, handler, false);
    }

    @Override
    public <Q, A> Subscription<Q, A> bridge(Topic<Q, A> topic, MessageHandler<Q, A> handler) {
        return newSubscription(topic, handler, true);
    }

    private <Q, A> Subscription<Q, A> newSubscription(Topic<Q, A> topic, MessageHandler<Q, A> handler, Boolean isBridged
    ) {
        class SubscriptionHolder implements Subscription<Q, A> {
            final List<Subscription<?, ?>> subscriptionList = openSubscriptions.compute(topic, (k, v) -> new ArrayList<>());
            private final long id = idCount.getAndIncrement();
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
            public A onEvent(Subject sender, Q publication) {
//                println("Handle event for subscription {id: $id to: $to isBridged: $isBridged} ")
                return handler.onMessage(
                        sender,
                        publication
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
    public <Q, A> void publishAsync(Topic<Q, A> to,
                                    Q payload,
                                    Consumer<A> handler) {
        AtomicReference<Subscription<?, ?>> subscriptionHolder = new AtomicReference<>();
        // Establish response subscription
        var replyTo =
                new Topic<>("com.loudsight.utilities.service.dispatcher.ParallelDispatcher.publishAsync",
                        "" + replyId++, to.responseType(), NullValue.class);

        var subscription = this.subscribe(replyTo, new MessageHandler<A, NullValue>() {
            @Override
            public NullValue onMessage(Subject sender, A payload) {
                subscriptionHolder.get().unsubscribe();
                handler.accept(payload);

                return NullValue.INSTANCE;
            }
        });
        subscriptionHolder.set(subscription);
        publish(to, replyTo, null, payload, BridgeMessageType.DIRECT_ASYNC);
    }
}