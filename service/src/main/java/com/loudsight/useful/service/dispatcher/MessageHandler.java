package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;

@FunctionalInterface
public interface MessageHandler<Q, A> {
    A onMessage(Subject sender, Q payload);
}