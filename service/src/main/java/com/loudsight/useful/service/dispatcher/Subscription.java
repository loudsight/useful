package com.loudsight.useful.service.dispatcher;

public interface Subscription<P, Q, A> {
        long getId();

        A onEvent(Envelope envelope);

        void unsubscribe();

        boolean isActive();

        boolean isBridged();
}
