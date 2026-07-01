package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Permission.class)
public record Permission(
    @Id Grant grant
) {}
