package com.loudsight.useful.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Supplier<T> newPoolEntry;
    private final Consumer<T> reset;

    public ObjectPool(Supplier<T> newPoolEntry, Consumer<T> reset) {
        this.newPoolEntry = newPoolEntry;
        this.reset = reset;
    }

    private final Deque<T> pool = new LinkedList<>();

    public T get() {
        var it = pool.pollFirst();

        try {
            if (it == null) {
                it = newPoolEntry.get();
            } else {
                return pool.removeFirst();
            }
        } catch (Exception e) {
            if (it != null) {
                put(it);
            }
        }
        return it;
    }

    public void put(T it) {
        reset.accept(it);
        pool.addLast(it);
    }
}
