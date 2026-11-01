package com.loudsight.useful.service.publisher;

import com.loudsight.useful.service.StubTimeProvider;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.SubscriptionHandle;
import com.loudsight.useful.service.dispatcher.Topic;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickPublisherTest {

    private static final class RecordingDispatcher implements Dispatcher {
        final List<Object> published = new CopyOnWriteArrayList<>();

        @Override
        public void close() {
            // nothing to release
        }

        @Override
        public <P, Q, A> SubscriptionHandle<P, Q, A> subscribe(Topic<P, Q, A> requestTopic,
                                                              @Nullable Topic<P, A, ?> responseTopic,
                                                              Function<Q, @Nullable A> handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <P, Q, A> void publish(Topic<P, Q, A> requestTopic, @Nullable Topic<?, A, ?> responseTopic, Q payload) {
            published.add(payload);
        }

        @Override
        public <P, Q, A> void publishAsync(Topic<P, Q, A> topic, Q payload, Consumer<@Nullable A> handler) {
            throw new UnsupportedOperationException();
        }
    }

    private static void awaitCount(RecordingDispatcher dispatcher, int count) throws InterruptedException {
        var deadline = System.nanoTime() + 1_000_000_000L;
        while (dispatcher.published.size() < count && System.nanoTime() < deadline) {
            Thread.sleep(5);
        }
    }

    @Test
    void publishesOneTickPerIntervalAndNoneBefore() throws InterruptedException {
        var start = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        var clock = new StubTimeProvider(start);
        var startMillis = clock.millisNow();
        var dispatcher = new RecordingDispatcher();

        try (var publisher = new TickPublisher(dispatcher, clock)) {
            assertNotNull(publisher);
            // interval has not elapsed: old code published continuously here (elapsed % 1000 == 0 was stuck true)
            Thread.sleep(150);
            assertEquals(0, dispatcher.published.size(), "no tick before the interval elapses");

            clock.increment(1000);
            awaitCount(dispatcher, 1);
            Thread.sleep(150);
            assertEquals(List.of(startMillis + 1000), dispatcher.published, "exactly one tick for one interval");

            clock.increment(1000);
            awaitCount(dispatcher, 2);
            Thread.sleep(150);
            assertEquals(List.of(startMillis + 1000, startMillis + 2000), dispatcher.published);
        }
    }

    @Test
    void doesNotPublishWhenClockNeverReachesBoundary() throws InterruptedException {
        var clock = new StubTimeProvider(LocalDateTime.now(ZoneOffset.UTC));
        var dispatcher = new RecordingDispatcher();

        try (var publisher = new TickPublisher(dispatcher, clock)) {
            assertNotNull(publisher);
            clock.increment(999);
            Thread.sleep(150);
            assertTrue(dispatcher.published.isEmpty());
        }
    }
}
