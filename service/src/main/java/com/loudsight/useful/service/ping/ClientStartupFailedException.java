package com.loudsight.useful.service.ping;

/**
 * Generic runtime exception thrown when a dependent service fails to acknowledge
 * the startup ping within the configured timeout budget.
 */
public class ClientStartupFailedException extends RuntimeException {

    private final String clientName;
    private final String serverName;
    private final long timeoutMs;
    private final long elapsedNanos;
    private final int attempts;

    public ClientStartupFailedException(String clientName,
                                        String serverName,
                                        long timeoutMs,
                                        long elapsedNanos,
                                        int attempts,
                                        Throwable cause) {
        super(buildMessage(clientName, serverName, timeoutMs, elapsedNanos, attempts), cause);
        this.clientName = clientName;
        this.serverName = serverName;
        this.timeoutMs = timeoutMs;
        this.elapsedNanos = elapsedNanos;
        this.attempts = attempts;
    }

    private static String buildMessage(String clientName,
                                       String serverName,
                                       long timeoutMs,
                                       long elapsedNanos,
                                       int attempts) {
        long elapsedMs = Math.max(0, elapsedNanos / 1_000_000);
        return "Client startup failed for %s (server=%s) after %dms timeout=%dms attempts=%d"
                .formatted(clientName, serverName, elapsedMs, timeoutMs, attempts);
    }

    public String getClientName() {
        return clientName;
    }

    public String getServerName() {
        return serverName;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public long getElapsedNanos() {
        return elapsedNanos;
    }

    public long getElapsedMs() {
        return Math.max(0, elapsedNanos / 1_000_000);
    }

    public int getAttempts() {
        return attempts;
    }
}
