package com.loudsight.useful.util;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Supplier<T> newPoolEntry;
    private final Consumer<T> reset;

    public ObjectPool(Supplier<T> newPoolEntry, Consumer<T> reset) {
        this.newPoolEntry = newPoolEntry;
        this.reset = reset;
    }

    private final Deque<T> pool = new ArrayDeque<>();

    public @Nullable T get() {
        var it = pool.pollFirst();
        if (it != null) {
            return it;
        }

        try {
            return newPoolEntry.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void put(T it) {
        reset.accept(it);
        pool.addLast(it);
    }
}
