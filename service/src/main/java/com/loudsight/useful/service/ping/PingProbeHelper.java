package com.loudsight.useful.service.ping;

import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Subscription;
import com.loudsight.useful.service.dispatcher.Topic;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Shared helper responsible for executing the ping-pong handshake against a remote service.
 * Handles subscription lifecycle, timeout management and retry logic so client implementations
 * can remain focused on wiring configuration.
 */
public final class PingProbeHelper {

    private PingProbeHelper() {
    }

    public record ProbeOutcome(boolean skipped,
                               PingRequest request,
                               PongResponse response,
                               int attempts,
                               long elapsedNanos) {

        public static ProbeOutcome skippedOutcome() {
            return new ProbeOutcome(true, null, null, 0, 0);
        }

        public long elapsedMillis() {
            return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        }
    }

    public static ProbeOutcome probeOrThrow(Dispatcher dispatcher,
                                            Topic<?, PingRequest, PongResponse> pingRequestTopic,
                                            Topic<?, PongResponse, ?> pingResponseTopic,
                                            PingProbeConfig config,
                                            Supplier<PingRequest> requestSupplier,
                                            String clientName,
                                            String serverName) {
        Objects.requireNonNull(dispatcher, "dispatcher");
        Objects.requireNonNull(pingRequestTopic, "pingRequestTopic");
        Objects.requireNonNull(pingResponseTopic, "pingResponseTopic");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(requestSupplier, "requestSupplier");

        if (!config.shouldProbe()) {
            return ProbeOutcome.skippedOutcome();
        }

        int attempts = Math.max(1, config.getRetryCount());
        long startNanos = System.nanoTime();
        Throwable lastError = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                PingRequest request = requestSupplier.get();
                PongResponse response = sendOnce(dispatcher, pingRequestTopic, pingResponseTopic, config, request);
                long elapsed = System.nanoTime() - startNanos;
                return new ProbeOutcome(false, request, response, attempt, elapsed);
            } catch (TimeoutException e) {
                lastError = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                long elapsed = System.nanoTime() - startNanos;
                throw new ClientStartupFailedException(clientName, serverName, config.getTimeoutMs(), elapsed, attempt, e);
            } catch (RuntimeException e) {
                lastError = e;
            }

            sleep(config.getRetryDelayMs());
        }

        long elapsed = System.nanoTime() - startNanos;
        throw new ClientStartupFailedException(clientName, serverName, config.getTimeoutMs(), elapsed, attempts, lastError);
    }

    private static PongResponse sendOnce(Dispatcher dispatcher,
                                         Topic<?, PingRequest, PongResponse> pingRequestTopic,
                                         Topic<?, PongResponse, ?> pingResponseTopic,
                                         PingProbeConfig config,
                                         PingRequest request) throws TimeoutException, InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PongResponse> responseRef = new AtomicReference<>();
        AtomicReference<Subscription<?, ?, ?>> subscriptionRef = new AtomicReference<>();

        try {
            var subscription = dispatcher.subscribe(pingResponseTopic, response -> {
                responseRef.set((PongResponse) response);
                latch.countDown();
                return null;
            });
            subscriptionRef.set(subscription);

            dispatcher.publish(pingRequestTopic, pingResponseTopic, request);

            if (!latch.await(config.getTimeoutMs(), TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("No PONG response within " + config.getTimeoutMs() + "ms");
            }
            return responseRef.get();
        } finally {
            var subscription = subscriptionRef.get();
            if (subscription != null) {
                subscription.unsubscribe();
            }
        }
    }

    private static void sleep(long delayMs) {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
