package com.loudsight.utilities.helper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class DateTimeHelper {
    public static LocalDateTime newDateWithoutMillis(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond());
    }

    public static LocalDateTime newDateWithoutTime(LocalDateTime dateTime) {
        return LocalDateTime.of(dateTime.getYear(),
                dateTime.getMonth(),
                dateTime.getDayOfMonth(),
                0,
                0,
                0);
    }

    public static long toUtcEpochSecond(LocalDateTime localDateTime) {
        return localDateTime.toEpochSecond(ZoneOffset.UTC);
    }

    public static long secondsBetween(LocalDateTime one, LocalDateTime two) {
        return one.until(two, ChronoUnit.SECONDS);
    }
}
