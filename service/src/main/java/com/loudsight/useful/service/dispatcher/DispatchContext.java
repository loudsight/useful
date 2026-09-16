package com.loudsight.useful.service.dispatcher;

import org.jspecify.annotations.Nullable;

public final class DispatchContext {
    private static final ThreadLocal<String> CALLER = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION_TOKEN = new ThreadLocal<>();

    private DispatchContext() {
    }

    public static void setCaller(@Nullable String caller) {
        CALLER.set(caller);
    }

    public static @Nullable String getCaller() {
        return CALLER.get();
    }

    public static void setSessionToken(@Nullable String sessionToken) {
        SESSION_TOKEN.set(sessionToken);
    }

    public static @Nullable String getSessionToken() {
        return SESSION_TOKEN.get();
    }

    public static void clear() {
        CALLER.remove();
        SESSION_TOKEN.remove();
    }
}
