package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Envelope.class)
public record Envelope(
        Topic replyTo,
        Object payload
) {


    public Envelope(Object payload) {
        this(null, payload);
    }

    public <T> T getCargo() {
        return (T) payload;
    }
}
