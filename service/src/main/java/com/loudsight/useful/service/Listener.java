package com.loudsight.useful.service;

import com.loudsight.useful.helper.ExceptionHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Listener<T> implements Consumer<T> {
    private final CompletableFuture<T> results = new CompletableFuture<>();
    public T getResult() {
        return getResult(60, TimeUnit.SECONDS);
    }

    public T getResult(long timeout, TimeUnit unit) {
        T res;

        try {
            res = results.get(timeout, unit);
        } catch (Exception e) {
            ExceptionHelper.uncheckedThrow(e);
            throw new RuntimeException("", e);
        }

        return res;
    }

    @Override
    public void accept(T result) {
        try {
            results.complete(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
