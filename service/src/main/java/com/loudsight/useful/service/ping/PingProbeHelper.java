package com.loudsight.useful.service.ping;

import com.loudsight.useful.helper.logging.LoggingHelper;
import com.loudsight.useful.service.dispatcher.Dispatcher;
import com.loudsight.useful.service.dispatcher.Subscription;
import com.loudsight.useful.service.dispatcher.Topic;

import java.lang.invoke.MethodHandles;
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

    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

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
            logger.logDebug("Probe skipped for {} -> {}", clientName, serverName);
            return ProbeOutcome.skippedOutcome();
        }

        int attempts = Math.max(1, config.getRetryCount());
        long startNanos = System.nanoTime();
        Throwable lastError = null;
        logger.logInfo("Starting probe for {} -> {} (attempts: {}, timeout: {}ms)", 
            clientName, serverName, attempts, config.getTimeoutMs());
        
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                PingRequest request = requestSupplier.get();
                logger.logDebug("Sending probe request (attempt {}/{})", attempt, attempts);
                PongResponse response = sendOnce(dispatcher, pingRequestTopic, pingResponseTopic, config, request);
                long elapsed = System.nanoTime() - startNanos;
                logger.logInfo("Probe succeeded for {} -> {} in {}ms (attempt {})", 
                    clientName, serverName, TimeUnit.NANOSECONDS.toMillis(elapsed), attempt);
                return new ProbeOutcome(false, request, response, attempt, elapsed);
            } catch (TimeoutException e) {
                lastError = e;
                logger.logWarn("Probe timeout for {} -> {} (attempt {}/{})", clientName, serverName, attempt, attempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                long elapsed = System.nanoTime() - startNanos;
                logger.logError("Probe interrupted for {} -> {} after {}ms", clientName, serverName, TimeUnit.NANOSECONDS.toMillis(elapsed), e);
                throw new ClientStartupFailedException(clientName, serverName, config.getTimeoutMs(), elapsed, attempt, e);
            } catch (RuntimeException e) {
                lastError = e;
                logger.logWarn("Probe error for {} -> {} (attempt {}/{}): {}", clientName, serverName, attempt, attempts, e.getMessage());
            }

            if (attempt < attempts) {
                logger.logDebug("Retrying probe in {}ms", config.getRetryDelayMs());
                sleep(config.getRetryDelayMs());
            }
        }

        long elapsed = System.nanoTime() - startNanos;
        logger.logError("Probe failed for {} -> {} after {} attempts in {}ms", 
            clientName, serverName, attempts, TimeUnit.NANOSECONDS.toMillis(elapsed));
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
                logger.logDebug("Pong received");
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
