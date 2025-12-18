package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.meta.MetaRepository;
import com.loudsight.useful.service.NamedThreadFactory;
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
import java.util.function.Function;

public class ParallelDispatcher implements Dispatcher, AutoCloseable {
    private static final List<Subscription<?, ?, ?>> EMPTY_LIST = new ArrayList<>();
    private final ExecutorService executorService;

    TopicFactory topicFactory;
    private long replyId = 0L;
    private final Map<Topic, List<Subscription<?, ?, ?>>> openSubscriptions = new HashMap<>();
    private final List<Long> closedSubscriptions = new ArrayList<>();
    private final AtomicLong idCount = new AtomicLong();

    public ParallelDispatcher(TopicFactory topicFactory, int workerCount) {
        this.topicFactory = topicFactory;
        this.executorService = Executors.newFixedThreadPool(workerCount, new NamedThreadFactory("ParallelDispatcher"));
    }

    public ParallelDispatcher(TopicFactory topicFactory) {
        this(topicFactory, Runtime.getRuntime().availableProcessors() / 2);
    }

    @Override
    public  <P, Q, A> void publish(Topic<P, Q, A> topic,
                                     Q publication) {
        publish(topic, ClassHelper.uncheckedCast(Topic.NO_REPLY), publication);
//    }
//
//    public  <P, Q, A> void publish(Topic<?, ?, ?> topic,
//                                   Topic<?, ?, ?> replyTo,
//                                   Q publication) {
    }

    @Override
    public <P, Q, A> void publish(Topic<P, Q, A> requestTopic, Topic<?, A, ?> responseTopic, Q publication) {
        processPublications(requestTopic, responseTopic, publication);
    }

    protected <Q> void processPublications(Topic<?, ?, ?> topic,
                                           Topic<?, ?, ?> replyTo,
                                           Q publication) {

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

            if (replyTo != Topic.NO_REPLY) {
                var response = it.onEvent(new Envelope(publication));

                Object res = (response == null) ? NullValue.INSTANCE : response;
                publish((Topic) replyTo, new Envelope(res));
            } else {
                if (publication instanceof Envelope envelope) {
                    it.onEvent(envelope);
                } else {
                    it.onEvent(new Envelope(publication));
                }
            }
            if (!it.isActive()) {
                closedSubscriptions.remove(it.getId());
                openSubscriptionsIt.remove();
            }
        }
    }

    @Override
    public <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> topic, Function<Q, A> handler) {
        return newSubscription(topic, handler, false);
    }

    @Override
    public <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, Topic<P, A, ?> responseTopic, Function<Q, A> handler) {
        return newSubscription(requestTopic,handler, false);
    }

    private <P, Q, A> Subscription<P, Q, A> newSubscription(Topic<P, Q, A> topic, Function<Q, A> handler, Boolean isBridged
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
            public A onEvent(Envelope envelope) {
//                println("Handle event for subscription {id: $id to: $to isBridged: $isBridged} ")
                return handler.apply(envelope.getCargo());
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
                        Map.of("id", "publishAsync" + replyId++));

        var subscription = this.subscribe(replyTo, (payload1) -> {
            subscriptionHolder.get().unsubscribe();
            handler.accept(payload1 == NullValue.INSTANCE? null : payload1);
            return  null;
        });
        subscriptionHolder.set(subscription);
        publish(to, replyTo, payload);
    }

    @Override
    public void close() throws Exception {
        executorService.shutdown();
    }
}