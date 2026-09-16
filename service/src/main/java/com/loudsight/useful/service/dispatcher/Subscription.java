package com.loudsight.useful.service.dispatcher;

import org.jspecify.annotations.Nullable;

public interface Subscription<P, Q, A> {
        long getId();

        @Nullable A onEvent(Envelope envelope);

        void unsubscribe();

        boolean isActive();

        boolean isBridged();
}
