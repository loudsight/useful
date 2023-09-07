package com.loudsight.useful.service.aeron;

import com.loudsight.meta.MetaRepository;
import com.loudsight.meta.entity.SimpleEntityMeta;
import com.loudsight.meta.serialization.EntityTransform;
import com.loudsight.useful.entity.permission.Subject;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.dispatcher.*;
import com.loudsight.useful.service.publisher.AeronTopicFactory;
import io.aeron.Aeron;
import io.aeron.Publication;
import org.agrona.concurrent.IdleStrategy;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.agrona.concurrent.UnsafeBuffer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class AeronDispatcher extends ParallelDispatcher implements AutoCloseable {

    static {
        MetaRepository.getInstance().register(SimpleEntityMeta.getInstance());
    }

    final IdleStrategy idle = new SleepingIdleStrategy();
    final UnsafeBuffer unsafeBuffer = new UnsafeBuffer(ByteBuffer.allocate(256));
    Aeron aeron;

    private final ExecutorService executorService;

    private volatile boolean isOpen = true;

    public AeronDispatcher(Aeron aeron, AeronTopicFactory topicFactory) {
        super(topicFactory);
        this.aeron = aeron;
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("AeronDispatcher"));

        executorService.submit(this::work);
    }

    void work() {
        try {
            boolean hasActiveSubscriptions = false;

            if (isOpen) {
                var ae = topicToSubscriptionMap.entrySet().iterator();

                while (ae.hasNext()) {
                    Map.Entry<Topic<?, ?, ?>, AeronSubscription<?, ?, ?>> xx = ae.next();
                    var sub = xx.getValue();

                    if (!sub.poll(it -> {
                        super.<Object, Object, Object>publish((Topic) xx.getKey(), null, it);
                    })) {
                        hasActiveSubscriptions = true;
                    }

                    if (!hasActiveSubscriptions) {
                        idle.idle();
                    }
                }
            }
        } finally {
            executorService.submit(this::work);
        }

    }

    @Override
    public void close() {
        isOpen = false;
        aeron.close();
        executorService.shutdown();
    }

    private final Map<Topic<?, ?, ?>, AeronSubscription<?, ?, ?>> topicToSubscriptionMap = new HashMap<>();
    private final AtomicInteger nextStreamId = new AtomicInteger(100);

    @Override
    public <P, Q, A> Subscription<P, Q, A> subscribe(Topic<P, Q, A> topic, MessageHandler<Q, A> handler) {
        var res = executorService.submit(() -> {
            var channel = topic.getPropertyOrDefault("channel", "aeron:ipc");
            var subscription = super.subscribe(topic, handler);

            topicToSubscriptionMap.computeIfAbsent(topic, k -> {
                var streamId = nextStreamId.getAndIncrement();
                io.aeron.Subscription sub = aeron.addSubscription(channel, streamId);

                return new AeronSubscription<>(sub, topic);
            });

            return subscription;
        });

        try {
            return res.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <P, Q, A> void publish(Topic<P, Q, A> topic, Subject publisher, Q payload) {
        executorService.submit(() -> {
            try {
                var channel = topic.getPropertyOrDefault("channel", "aeron:ipc");
                var streamId = topicToSubscriptionMap.get(topic);

                Publication pub = aeron.addPublication(channel, streamId.aeronSubscription().streamId());
                while (!pub.isConnected()) {
                    idle.idle();
                }
                var bytes = EntityTransform.serialize(payload);
                unsafeBuffer.putBytes(0, bytes);

                while (pub.offer(unsafeBuffer) < 0) {
                    idle.idle();
                }
            } catch (Exception e) {
                idle.idle();
            }
        });
    }
}
