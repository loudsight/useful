package com.loudsight.useful.service.dispatcher;

import java.util.Objects;

/**
 * Lightweight handle exposed to callers so they can manage the lifecycle of a dispatcher subscription
 * without depending on the internal {@link Subscription} surface area.
 */
public final class SubscriptionHandle<P, Q, A> {

    private final Subscription<P, Q, A> delegate;

    public SubscriptionHandle(Subscription<P, Q, A> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    public void unsubscribe() {
        delegate.unsubscribe();
    }

    Subscription<P, Q, A> delegate() {
        return delegate;
    }
}
