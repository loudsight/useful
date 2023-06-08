package com.loudsight.useful.service.publisher;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.TimeProvider;
import com.loudsight.useful.service.dispatcher.Address;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Topic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides a basic implementation of the a 'Tick Publisher'
 * The publisher produces ticks on fixed addresses
 *
 * @author  Munya M.
 */
public class TickPublisher implements AutoCloseable {
    public static final Topic<Long, Object> ONE_SECOND_TICK = new Topic<>(
            new Address("com.loudsight.useful.service.publisher.TickPublisher", "ONE_SECOND_TICK"),
            Long.class,
            Object.class
    );

    private volatile boolean isOpen = true;
    Dispatcher dispatcher;
    TimeProvider timeProvider;
    private static final LoggingHelper logger = LoggingHelper.wrap(TickPublisher.class);
    private final long startTime;
    ExecutorService executor;
        public TickPublisher(Dispatcher dispatcher,
                             TimeProvider timeProvider) {
            this.dispatcher = dispatcher;
            this.timeProvider = timeProvider;
            this.startTime = timeProvider.millisNow();
            this.executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("TickPublisher"));
            this.executor.submit(this::execute);
        }

    public TickPublisher(Dispatcher dispatcher) {
        this(dispatcher, TimeProvider.DEFAULT);
    }

    private void execute() {
        var currentTimeMillis = timeProvider.millisNow();
        logger.debug("-Tick produced at " + currentTimeMillis);

        while (isOpen) {
            if ((currentTimeMillis - startTime) % 1000 == 0) {
                dispatcher.publish(ONE_SECOND_TICK, null, currentTimeMillis);
            } else {
                Thread.yield();
            }
        }
    }

    @Override
    public void close() throws Exception {
        isOpen = false;
    }
}
