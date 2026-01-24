package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.helper.ClassHelper;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.publisher.TopicFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelDispatcher.class);
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
    public void close() {
        executorService.shutdown();
    }

    @Override
    public  <P, Q, A> void publish(Topic<P, Q, A> topic, Q publication) {
        publish(topic, ClassHelper.uncheckedCast(Topic.NO_REPLY), publication);
    }

    @Override
    public <P, Q, A> void publish(Topic<P, Q, A> requestTopic, Topic<?, A, ?> responseTopic, Q publication) {
        processPublications(requestTopic, responseTopic, publication);
    }

    protected <Q> void processPublications(Topic<?, ?, ?> topic,
                                           Topic<?, ?, ?> replyTo,
                                           Q publication) {
		String debugId = "processPublications-" + System.currentTimeMillis() + "-" + topic;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] processPublications() CALLED on thread {}, topic={}, replyTo={}"
					, debugId, Thread.currentThread().getName(), topic, replyTo);
		}

		List<Subscription<?, ?, ?>> subscriptions = new ArrayList<>();
		subscriptions.addAll(openSubscriptions.getOrDefault(topic, EMPTY_LIST));
		subscriptions.addAll(openSubscriptions.getOrDefault(Topic.WILDCARD_ADDRESS, EMPTY_LIST));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] Found {} subscriptions", debugId, subscriptions.size());
		}
		var openSubscriptionsIt = subscriptions.iterator();

		while (openSubscriptionsIt.hasNext()) {
			var it = openSubscriptionsIt.next();

			if (!it.isActive()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[{}] Subscription inactive, skipping", debugId);
				}
//                openSubscriptionsIt.remove()
//                closedSubscriptions.remove(it.getId())
				continue;
			}

			if (replyTo != Topic.NO_REPLY) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[{}] Processing with reply: calling it.onEvent()...", debugId);
				}
				var response = it.onEvent(new Envelope(publication));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[{}] it.onEvent() returned response type: {}", debugId,
						response == null ? "null" : response.getClass().getSimpleName());
				}

				Object res = (response == null) ? NullValue.INSTANCE : response;
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[{}] Publishing response to replyTo topic...", debugId);
				}
				publish((Topic) replyTo, new Envelope(res));
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[{}] Response published", debugId);
				}
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] processPublications() COMPLETE", debugId);
		}
    }

    @Override
    public <P, Q, A> SubscriptionHandle<P, Q, A> subscribe(Topic<P, Q, A> topic, Function<Q, A> handler) {
        return new SubscriptionHandle<>(newSubscription(topic, handler, false));
    }

    @Override
    public <P, Q, A> SubscriptionHandle<P, Q, A> subscribe(Topic<P, Q, A> requestTopic, Topic<P, A, ?> responseTopic, Function<Q, A> handler) {
        return new SubscriptionHandle<>(newSubscription(requestTopic,handler, false));
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
		String debugId = "publishAsync-" + System.currentTimeMillis() + "-" + to;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] publishAsync() CALLED on thread {}", debugId, Thread.currentThread().getName());
		}
		AtomicReference<SubscriptionHandle<?, ?, ?>> subscriptionHolder = new AtomicReference<>();
		var replyTo =
			new Topic<>(ParallelDispatcher.class,
				to.responseType(), NullValue.class,
				Map.of("id", "publishAsync" + replyId++));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[{}] Subscribing to replyTo topic", debugId);
		}
		SubscriptionHandle<?, ?, ?> subscription = this.subscribe(replyTo, (payload1) -> {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[{}] Response handler INVOKED with payload type: {}", debugId,
					payload1 == null ? "null" : payload1.getClass().getSimpleName());
			}
	            subscriptionHolder.get().unsubscribe();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] Calling user handler...", debugId);
            }
            handler.accept(payload1 == NullValue.INSTANCE ? null : payload1);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[{}] User handler completed", debugId);
            }
            return null;
        });
		subscriptionHolder.set(subscription);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] Publishing to request topic...", debugId);
        }
        publish(to, replyTo, payload);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[{}] publishAsync() COMPLETE", debugId);
        }
    }
}