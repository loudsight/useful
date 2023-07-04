package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;

public interface Subscription<P, Q, A> {
        long getId();

        A onEvent(Subject sender, Q payload);

        void unsubscribe();

        boolean isActive();

        boolean isBridged();
}
