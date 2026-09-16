package com.loudsight.useful.service;

import com.loudsight.useful.helper.ExceptionHelper;
import com.loudsight.useful.helper.logging.LoggingHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class Listener<T> implements Consumer<@Nullable T> {
    private static final LoggingHelper logger = LoggingHelper.wrap(Listener.class);
    private final CompletableFuture<@Nullable T> results = new CompletableFuture<>();
    public @Nullable T getResult() {
        return getResult(60, TimeUnit.SECONDS);
    }

    public @Nullable T getResult(long timeout, TimeUnit unit) {
        logger.logDebug("[EVIDENCE] Listener.getResult(" + timeout + " " + unit + ") called, results.isDone=" + results.isDone());
        @Nullable T res;

        try {
            logger.logDebug("[EVIDENCE] About to call results.get(" + timeout + ", " + unit + ")...");
            res = results.get(timeout, unit);
            logger.logDebug("[EVIDENCE] results.get() returned successfully: " + res);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.logDebug("[EVIDENCE] results.get() threw exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            ExceptionHelper.uncheckedThrow(e);
            throw new IllegalStateException("Failed to get result", e);
        }

        return res;
    }

    @Override
    public void accept(@Nullable T result) {
        logger.logDebug("[EVIDENCE] Listener.accept() called with result: " + result);
        try {
            results.complete(result);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
