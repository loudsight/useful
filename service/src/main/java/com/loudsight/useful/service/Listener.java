package com.loudsight.useful.service;

import com.loudsight.useful.helper.ExceptionHelper;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Listener<T> implements Consumer<T> {

    private final Deque<T> results = new LinkedBlockingDeque<>();
    public T getResult() {
        return getResult(10, TimeUnit.SECONDS);
    }

    public T getResult(long timeout, TimeUnit unit) {
        T res = null;
        while (res == null) {
            try {

                res = results.poll();
            } catch (Exception e) {
                ExceptionHelper.uncheckedThrow(e);
                throw new RuntimeException("", e);
            }
        }
        return res;
    }

    @Override
    public void accept(T result) {
        results.addLast(result);
    }
}
