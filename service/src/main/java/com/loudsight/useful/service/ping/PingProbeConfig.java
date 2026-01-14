package com.loudsight.useful.service.ping;

/**
 * Shared configuration contract used by client-specific {@code @ConfigurationProperties}
 * implementations to provide probe behavior without duplicating option names.
 */
public interface PingProbeConfig {

    boolean isEnabled();

    long getTimeoutMs();

    int getRetryCount();

    long getRetryDelayMs();

    default boolean shouldProbe() {
        return isEnabled() && getTimeoutMs() > 0;
    }
}
