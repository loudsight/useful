package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Envelope.class)
public record Envelope(
        Topic replyTo,
        Object payload,
        String caller,
        String sessionToken
) {


    public Envelope(Topic replyTo, Object payload) {
        this(replyTo, payload, null, null);
    }

    public Envelope(Object payload) {
        this(null, payload, null, null);
    }

    public Envelope(Object payload, String caller) {
        this(null, payload, caller, null);
    }

    public Envelope(Object payload, String caller, String sessionToken) {
        this(null, payload, caller, sessionToken);
    }

    public Envelope(Topic replyTo, Object payload, String caller) {
        this(replyTo, payload, caller, null);
    }

    public <T> T getCargo() {
        return (T) payload;
    }
}
