package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Role.class)
public class Role {
    @Id
    private final String name;

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
