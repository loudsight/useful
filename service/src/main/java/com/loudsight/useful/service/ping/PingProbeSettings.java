package com.loudsight.useful.service.ping;

/**
 * Immutable {@link PingProbeConfig} implementation that can be instantiated directly or
 * via framework configuration (e.g. Spring {@code @Value}) without introducing any
 * dependency on Spring Boot.
 */
public final class PingProbeSettings implements PingProbeConfig {

    private final boolean enabled;
    private final long timeoutMs;
    private final int retryCount;
    private final long retryDelayMs;

    public PingProbeSettings(boolean enabled, long timeoutMs, int retryCount, long retryDelayMs) {
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("timeoutMs must be >= 0");
        }
        if (retryCount < 0) {
            throw new IllegalArgumentException("retryCount must be >= 0");
        }
        if (retryDelayMs < 0) {
            throw new IllegalArgumentException("retryDelayMs must be >= 0");
        }
        this.enabled = enabled;
        this.timeoutMs = timeoutMs;
        this.retryCount = retryCount;
        this.retryDelayMs = retryDelayMs;
    }

    public static PingProbeSettings disabled() {
        return new PingProbeSettings(false, 0, 0, 0);
    }

    public static PingProbeSettings enabledWithDefaults() {
        return new PingProbeSettings(true, 30_000, 3, 1_000);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public long getTimeoutMs() {
        return timeoutMs;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public PingProbeSettings withEnabled(boolean enabled) {
        return new PingProbeSettings(enabled, timeoutMs, retryCount, retryDelayMs);
    }

    public PingProbeSettings withTimeoutMs(long timeoutMs) {
        return new PingProbeSettings(enabled, timeoutMs, retryCount, retryDelayMs);
    }

    public PingProbeSettings withRetryCount(int retryCount) {
        return new PingProbeSettings(enabled, timeoutMs, retryCount, retryDelayMs);
    }

    public PingProbeSettings withRetryDelayMs(long retryDelayMs) {
        return new PingProbeSettings(enabled, timeoutMs, retryCount, retryDelayMs);
    }
}
