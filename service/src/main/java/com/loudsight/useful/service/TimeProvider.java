package com.loudsight.useful.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Every LocalDateTime in this system is a UTC instant - that is the storage contract, not a
 * convention. Persistence writes them as {@code datetime('...')} with no offset, which Memgraph
 * reads as UTC (CypherQueryUtils), TemporalEntityTransform serializes them via
 * {@code atZone(ZoneOffset.UTC)}, and {@link #millis} below reads them back the same way.
 * <p>
 * So they must be *created* in UTC too. A bare {@code LocalDateTime.now()} reads the JVM default
 * zone, and every one of those boundaries then relabels that wall-clock reading as UTC rather
 * than converting it - silently shifting the instant by the JVM's offset. Always obtain "now"
 * from here, or from {@code LocalDateTime.now(ZoneOffset.UTC)} in modules that cannot depend on
 * this one.
 */
@FunctionalInterface
public interface TimeProvider {

    LocalDateTime now();

    default long millisNow() {
        return millis(now());
    }

    default long millis(LocalDateTime date) {
        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    TimeProvider DEFAULT = () -> LocalDateTime.now(ZoneOffset.UTC);
}
