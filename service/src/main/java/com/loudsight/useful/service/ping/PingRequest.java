package com.loudsight.useful.service.ping;

import com.loudsight.meta.annotation.Introspect;

import java.util.Objects;
import java.util.UUID;

/**
 * Lightweight payload sent by clients to verify that the remote service
 * backing a Dispatcher topic is reachable before the application finishes
 * starting up.
 *
 * @param probeId        Correlation identifier echoed by the server.
 * @param issuedAtNanos  Monotonic timestamp captured via {@link System#nanoTime()}.
 */
@Introspect(clazz = PingRequest.class)
public record PingRequest(String probeId, long issuedAtNanos) {

    public PingRequest {
        Objects.requireNonNull(probeId, "probeId");

        if (issuedAtNanos <= 0) {
            throw new IllegalArgumentException("issuedAtNanos must be > 0");
        }
    }

    /**
     * Creates a request with a random probe id that includes the provided prefix.
     */
    public static PingRequest forClient(String prefix) {
        return new PingRequest(prefix + "-" + UUID.randomUUID(), System.nanoTime());
    }

    /**
     * Creates a request with the provided probe id and the current monotonic timestamp.
     */
    public static PingRequest create(String probeId) {
        return new PingRequest(probeId, System.nanoTime());
    }
}
