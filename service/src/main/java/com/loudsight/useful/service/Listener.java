package com.loudsight.useful.service;

import com.loudsight.useful.helper.ExceptionHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Listener<T> implements Consumer<T> {

    private final AtomicReference<T> resultHolder = new AtomicReference<>();
    private final CountDownLatch resultCountdown = new CountDownLatch(1);

    public T getResult() {
        return getResult(10, TimeUnit.SECONDS);
    }

    public T getResult(long timeout, TimeUnit unit) {
        boolean res = false;
        while (!res) {
            try {

                res = resultCountdown.await(timeout, unit);
            } catch (InterruptedException ignored) {
                // will loop

            } catch (Exception e) {
                ExceptionHelper.uncheckedThrow(e);
                throw new RuntimeException("", e);
            }
        }
        return resultHolder.get();
    }

    @Override
    public void accept(T result) {
        resultHolder.set(result);
        resultCountdown.countDown();
    }
}
