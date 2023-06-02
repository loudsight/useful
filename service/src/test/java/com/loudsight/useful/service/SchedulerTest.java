package com.loudsight.useful.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchedulerTest {
    private final TestTimeProvider timeProvider = new TestTimeProvider(LocalDateTime.now());
    private final long startTime = timeProvider.millisNow();
    private Scheduler scheduler = new Scheduler();

    @Test
    public void canExecuteImmediately() {
        long lastRefresh = 0;
        long updateInterval = 100;
        assertTrue(scheduler.canExecute(startTime, lastRefresh, currentTime()));

        lastRefresh = scheduler.getNextExecutionPoint(startTime, currentTime(), updateInterval);
        assertFalse(scheduler.canExecute(startTime, lastRefresh, currentTime()));

        timeProvider.increment(100);
        assertTrue(scheduler.canExecute(startTime, lastRefresh, currentTime()));
    }

    private long currentTime() {
        return timeProvider.millisNow();
    }
}