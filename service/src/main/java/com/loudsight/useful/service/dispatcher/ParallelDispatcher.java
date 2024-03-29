package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.MetaRepository;
import com.loudsight.collection.Pair;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.helper.ClassHelper;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageType;
import com.loudsight.useful.service.dispatcher.bridge.BridgeMessageTypeMeta;
import com.loudsight.useful.service.publisher.TopicFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ParallelDispatcher implements Dispatcher, AutoCloseable {
    private static final List<Subscription<?, ?, ?>> EMPTY_LIST = new ArrayList<>();
    private final ExecutorService executorService;

    TopicFactory topicFactory;
    private long replyId = 0L;
    private final Map<Topic, List<Subscription<?, ?, ?>>> openSubscriptions = new HashMap<>();
    private final List<Long> closedSubscriptions = new ArrayList<>();
    private final AtomicLong idCount = new AtomicLong();

    static {
        MetaRepository.getInstance().register(AddressMeta.getInstance());
        MetaRepository.getInstance().register(BridgeMessageTypeMeta.getInstance());
        MetaRepository.getInstance().register(PublicationMeta.getInstance());
    }

    public ParallelDispatcher(TopicFactory topicFactory, int workerCount) {
        this.topicFactory = topicFactory;
        this.executorService = Executors.newFixedThreadPool(workerCount, new NamedThreadFactory("ParallelDispatcher"));
    }

    public ParallelDispatcher(TopicFactory topicFactory) {
        this(topicFactory, Runtime.getRuntime().availableProcessors() / 2);
    }

    @Override
    public <P, Q, A> void publish(Topic<P, Q, A> topic,
                                  Subject publisher,
                                  Q payload) {
        publish(topic, Topic.NO_REPLY, publisher, payload, BridgeMessageType.DIRECT);
    }

    protected <P, Q, A> void publish(Topic<P, Q, A> topic,
                                     Subject publisher,
                                     Q payload,
                                     BridgeMessageType publicationType) {
        publish(topic, Topic.NO_REPLY, publisher, payload, publicationType);
    }

    private <P, Q, A> void publish(Topic<?, ?, ?> topic,
                                   Topic<?, ?, ?> replyTo,
                                   Subject publisher,
                                   Q publication,
                                   BridgeMessageType publicationType) {
        processPublications(topic, replyTo, publisher, publication, publicationType);
    }

    protected <Q> void processPublications(Topic<?, ?, ?> topic,
                                           Topic<?, ?, ?> replyTo,
                                           Subject publisher,
                                           Q publication,
                                           BridgeMessageType publicationType) {

        List<Subscription<?, ?, ?>> subscriptions = new ArrayList<>();
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
                        ClassHelper.uncheckedCast(publication)
                );
            } else if (publication == Dispatcher.BRIDGE_RETURN) {

            } else if (replyTo != Topic.NO_REPLY) {
                var response = it.onEvent(
                        publisher,
                        ClassHelper.uncheckedCast(publication)
                );

                if (response != Dispatcher.BRIDGE_RETURN) {
                    Object res = (response == null) ? NullValue.INSTANCE : response;
                    publish((Topic) replyTo, publisher, res, publicationType);
                }
            } else {
                it.onEvent(
                        publisher,
                        ClassHelper.uncheckedCast(publication)
                );
            }
            if (!it.isActive()) {
                closedSubscriptions.remove(it.getId());
                openSubscriptionsIt.remove();
            }
        }
    }

    @Override
    public <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> topic, MessageHandler<Q, A> handler) {
        return newSubscription(topic, handler, false);
    }

    @Override
    public <P, Q, A> Subscription<P, Q, A> bridge(Topic<P, Q, A> topic, MessageHandler<Q, A> handler) {
        return newSubscription(topic, handler, true);
    }

    private <P, Q, A> Subscription<P, Q, A> newSubscription(Topic<P, Q, A> topic, MessageHandler<Q, A> handler, Boolean isBridged
    ) {
        class SubscriptionHolder implements Subscription<P, Q, A> {
            final List<Subscription<?, ?, ?>> subscriptionList = openSubscriptions.compute(topic, (k, v) -> new ArrayList<>());
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
    public <P, Q, A> void publishAsync(Topic<P, Q, A> to,
                                       Q payload,
                                       Consumer<A> handler) {
        AtomicReference<Subscription<?, ?, ?>> subscriptionHolder = new AtomicReference<>();
        // Establish response subscription
        var replyTo =
                new Topic<>(ParallelDispatcher.class,
                        to.responseType(), NullValue.class,
                        List.of(Pair.of("id", "publishAsync" + replyId++)));

        var subscription = this.subscribe(replyTo, (sender, payload1) -> {
            subscriptionHolder.get().unsubscribe();
            handler.accept(payload1);

            return NullValue.INSTANCE;
        });
        subscriptionHolder.set(subscription);
        publish(to, replyTo, null, payload, BridgeMessageType.DIRECT_ASYNC);
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}