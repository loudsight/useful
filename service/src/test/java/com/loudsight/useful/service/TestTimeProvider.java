package com.loudsight.useful.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

public class TestTimeProvider implements  TimeProvider {

    private final AtomicLong time = new AtomicLong(0);
    private final LocalDateTime startTime;

    public TestTimeProvider(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public LocalDateTime now() {
        Instant instant = Instant.ofEpochMilli(millis(startTime) + time.get());

        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public void increment(long millis) {
        this.time.addAndGet(millis);
    }
}
