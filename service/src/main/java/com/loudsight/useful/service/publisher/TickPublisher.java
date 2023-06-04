package com.loudsight.useful.service.publisher;

import com.loudsight.useful.service.NamedThreadFactory;
import com.loudsight.useful.service.TimeProvider;
import com.loudsight.useful.service.dispatcher.Address;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Publication;
import com.loudsight.useful.service.dispatcher.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides a basic implementation of the {@link Publisher} interface
 * A publisher listens for subscription requests on a known address and send publications to the addresses
 * specified by subscribers in their subscription requests
 *
 * @author  Munya M.
 */
public class TickPublisher implements AutoCloseable {
    public static final Topic<Object, Long> ONE_SECOND_TICK = new Topic<>(
            new Address("com.loudsight.useful.service.publisher.TickPublisher", "ONE_SECOND_TICK"),
            Object.class,
            Long.class
    );

    private volatile boolean isOpen = true;
    Dispatcher dispatcher;
    TimeProvider timeProvider;
    private static final Logger logger = LoggerFactory.getLogger(TickPublisher.class);
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
        logger.debug("-execute $currentTimeMillis ${subscribers.values}");

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
