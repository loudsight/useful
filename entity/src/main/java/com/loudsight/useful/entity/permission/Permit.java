package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Permit.class)
public class Permit {
    private final String name;

    public Permit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}