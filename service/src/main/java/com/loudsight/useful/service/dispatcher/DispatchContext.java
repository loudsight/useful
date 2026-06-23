package com.loudsight.useful.service.dispatcher;

public final class DispatchContext {
    private static final ThreadLocal<String> CALLER = new ThreadLocal<>();
    private static final ThreadLocal<String> SESSION_TOKEN = new ThreadLocal<>();

    private DispatchContext() {
    }

    public static void setCaller(String caller) {
        CALLER.set(caller);
    }

    public static String getCaller() {
        return CALLER.get();
    }

    public static void setSessionToken(String sessionToken) {
        SESSION_TOKEN.set(sessionToken);
    }

    public static String getSessionToken() {
        return SESSION_TOKEN.get();
    }

    public static void clear() {
        CALLER.remove();
        SESSION_TOKEN.remove();
    }
}
