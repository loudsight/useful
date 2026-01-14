package com.loudsight.useful.service.ping;

import com.loudsight.meta.annotation.Introspect;

import java.util.Objects;

/**
 * Response emitted by a remote service acknowledging that it is online
 * and capable of handling Dispatcher traffic.
 *
 * @param probeId        Identifier echoed from {@link PingRequest}.
 * @param respondedAtNanos Monotonic timestamp captured with {@link System#nanoTime()}.
 * @param instanceId     Optional instance identifier emitted by the server (hostname, pod id, etc).
 */
@Introspect(clazz = PongResponse.class)
public record PongResponse(String probeId, long respondedAtNanos, String instanceId) {

    public PongResponse {
        Objects.requireNonNull(probeId, "probeId");
        if (respondedAtNanos <= 0) {
            throw new IllegalArgumentException("respondedAtNanos must be > 0");
        }
    }

    public static PongResponse from(PingRequest request, String instanceId) {
        return new PongResponse(request.probeId(), System.nanoTime(), instanceId);
    }
}
