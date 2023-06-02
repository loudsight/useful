package com.loudsight.useful.service;

public class Scheduler{
    public boolean canExecute(long startTime, long nextExecutionPoint, long currentTimeMillis) {
        long currentExecutionPoint = getCurrentExecutionPoint(startTime, currentTimeMillis);

        return nextExecutionPoint <= currentExecutionPoint;
    }

    public long getNextExecutionPoint(long startTime, long currentTimeMillis, long updateInterval) {
        long currentExecutionPoint = getCurrentExecutionPoint(startTime, currentTimeMillis);

        return currentExecutionPoint + updateInterval;
    }

    private long getCurrentExecutionPoint(long startTime, long currentTimeMillis) {
        return (currentTimeMillis - startTime);
    }

    public long convertExecutionPointToTime(long startTime, long currentExecutionPoint) {
        return  currentExecutionPoint + startTime;
    }
}