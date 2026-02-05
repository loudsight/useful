package com.loudsight.useful.service;

import com.loudsight.useful.helper.ExceptionHelper;
import com.loudsight.useful.helper.logging.LoggingHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Listener<T> implements Consumer<T> {
    private static final LoggingHelper logger = LoggingHelper.wrap(Listener.class);
    private final CompletableFuture<T> results = new CompletableFuture<>();
    public T getResult() {
        return getResult(60, TimeUnit.SECONDS);
    }

    public T getResult(long timeout, TimeUnit unit) {
        logger.logDebug("[EVIDENCE] Listener.getResult(" + timeout + " " + unit + ") called, results.isDone=" + results.isDone());
        T res;

        try {
            logger.logDebug("[EVIDENCE] About to call results.get(" + timeout + ", " + unit + ")...");
            res = results.get(timeout, unit);
            logger.logDebug("[EVIDENCE] results.get() returned successfully: " + res);
        } catch (Exception e) {
            logger.logDebug("[EVIDENCE] results.get() threw exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            ExceptionHelper.uncheckedThrow(e);
            throw new RuntimeException("", e);
        }

        return res;
    }

    @Override
    public void accept(T result) {
        logger.logDebug("[EVIDENCE] Listener.accept() called with result: " + result);
        try {
            results.complete(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
