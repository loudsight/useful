package com.loudsight.useful.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class ObjectPoolTest {
    private final AtomicInteger created = new AtomicInteger();
    private final AtomicInteger resets = new AtomicInteger();

    private ObjectPool<List<String>> newPool() {
        return new ObjectPool<>(() -> {
            created.incrementAndGet();
            return new ArrayList<>();
        }, l -> {
            resets.incrementAndGet();
            l.clear();
        });
    }

    @Test
    void getRemovesExactlyOneOfManyPooledItems() {
        var pool = newPool();
        var a = new ArrayList<String>();
        var b = new ArrayList<String>();
        var c = new ArrayList<String>();
        pool.put(a);
        pool.put(b);
        pool.put(c);

        assertSame(a, pool.get());
        // b and c must both still be pooled, in order, with nothing created
        assertSame(b, pool.get());
        assertSame(c, pool.get());
        assertEquals(0, created.get());

        // pool now empty: next get creates a new entry
        var fresh = pool.get();
        assertNotSame(c, fresh);
        assertEquals(1, created.get());
    }

    @Test
    void returnedItemIsNotStillPooled() {
        var pool = newPool();
        var a = new ArrayList<String>();
        var b = new ArrayList<String>();
        pool.put(a);
        pool.put(b);

        var first = pool.get();
        var second = pool.get();
        assertNotSame(first, second);
    }

    @Test
    void singlePooledItemIsReturnedAndPoolThenEmpty() {
        var pool = newPool();
        var a = new ArrayList<String>();
        pool.put(a);

        assertSame(a, pool.get());
        assertEquals(0, created.get());

        var next = pool.get();
        assertNotSame(a, next);
        assertEquals(1, created.get());
    }

    @Test
    void emptyPoolCreatesNewEntry() {
        var pool = newPool();
        pool.get();
        assertEquals(1, created.get());
    }

    @Test
    void putResetsItem() {
        var pool = newPool();
        var a = new ArrayList<String>();
        a.add("dirty");
        pool.put(a);

        assertEquals(1, resets.get());
        assertSame(a, pool.get());
        assertEquals(0, a.size());
    }
}
