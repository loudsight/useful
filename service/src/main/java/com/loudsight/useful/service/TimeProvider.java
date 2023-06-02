package com.loudsight.useful.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public interface TimeProvider {

    LocalDateTime now();

    default long millisNow() {
        return millis(now());
    }

    default long millis(LocalDateTime date) {
        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    TimeProvider DEFAULT = LocalDateTime::now;
}
