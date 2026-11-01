package com.loudsight.useful.service.publisher;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.TimeProvider;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Topic;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class provides a basic implementation of the a 'Tick Publisher'
 * The publisher produces ticks on fixed addresses
 */
public class TickPublisher implements AutoCloseable {
    public static final Topic<TickPublisher, Long, Object> ONE_SECOND_TICK = new Topic<>(
            TickPublisher.class,
            Long.class,
            Object.class,
            Map.of(
                    "service", "tick",
                    "name", "ONE_SECOND_TICK",
                    "streamId", 400,
                    "channel", "aeron:ipc"
            )
    );

    private static final long TICK_INTERVAL_MILLIS = 1000;
    private static final long MAX_POLL_MILLIS = 20;

    private volatile boolean isOpen = true;
    Dispatcher dispatcher;
    TimeProvider timeProvider;
    private static final LoggingHelper logger = LoggingHelper.wrap(TickPublisher.class);
    private final long startTime;
    ExecutorService executor;
    private final Future<?> tickTask;
        public TickPublisher(Dispatcher dispatcher,
                             TimeProvider timeProvider) {
            this.dispatcher = dispatcher;
            this.timeProvider = timeProvider;
            this.startTime = timeProvider.millisNow();
            this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("TickPublisher"));
            this.tickTask = this.executor.submit(this::execute);
        }

    public TickPublisher(Dispatcher dispatcher) {
        this(dispatcher, TimeProvider.DEFAULT);
    }

    private void execute() {
        var nextTick = startTime + TICK_INTERVAL_MILLIS;

        while (isOpen) {
            var currentTimeMillis = timeProvider.millisNow();
            if (currentTimeMillis >= nextTick) {
                logger.logDebug("-Tick produced at " + currentTimeMillis);
                dispatcher.publish(ONE_SECOND_TICK, currentTimeMillis);
                // one tick per elapsed interval; skip any intervals missed while stalled
                nextTick = startTime
                        + ((currentTimeMillis - startTime) / TICK_INTERVAL_MILLIS + 1) * TICK_INTERVAL_MILLIS;
            } else {
                try {
                    Thread.sleep(Math.min(nextTick - currentTimeMillis, MAX_POLL_MILLIS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void close() {
        isOpen = false;
        tickTask.cancel(true);
        executor.shutdownNow();
    }
}
