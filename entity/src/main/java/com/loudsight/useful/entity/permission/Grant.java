package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Grant.class)
public enum Grant {
    CAN_READ,
    CAN_WRITE,
    CAN_DELETE
}
