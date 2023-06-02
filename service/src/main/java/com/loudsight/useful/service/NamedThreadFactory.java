package com.loudsight.useful.service;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicInteger threadCount;

    public NamedThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
        this.threadCount = new AtomicInteger(1);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(namePrefix + "-" + threadCount.getAndIncrement());

        return thread;
    }
}
