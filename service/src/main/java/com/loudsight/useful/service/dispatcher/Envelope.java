package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;
import com.loudsight.useful.helper.ClassHelper;
import org.jspecify.annotations.Nullable;

@Introspect(clazz = Envelope.class)
public record Envelope(
    @Nullable Topic<?, ?, ?> replyTo,
        Object payload,
        @Nullable String caller,
        @Nullable String sessionToken
) {


    public Envelope(@Nullable Topic<?, ?, ?> replyTo, Object payload) {
        this(replyTo, payload, null, null);
    }

    public Envelope(Object payload) {
        this(null, payload, null, null);
    }

    public Envelope(Object payload, @Nullable String caller) {
        this(null, payload, caller, null);
    }

    public Envelope(Object payload, @Nullable String caller, @Nullable String sessionToken) {
        this(null, payload, caller, sessionToken);
    }

    public Envelope(@Nullable Topic<?, ?, ?> replyTo, Object payload, @Nullable String caller) {
        this(replyTo, payload, caller, null);
    }

    // Caller-driven cast: the payload type is only known at the call site.
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T getCargo() {
        return ClassHelper.uncheckedCast(payload);
    }
}
