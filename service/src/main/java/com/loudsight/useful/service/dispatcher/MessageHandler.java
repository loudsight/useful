package com.loudsight.useful.service.dispatcher;

import com.loudsight.useful.entity.permission.Subject;

public interface MessageHandler<Q, A> {
    A onMessage(Address to,
                Subject sender,
                Q payload);
}