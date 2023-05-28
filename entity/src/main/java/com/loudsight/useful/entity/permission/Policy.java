package com.loudsight.useful.entity.permission;

import java.util.Arrays;
import java.util.Collection;

public class Policy {
    private final Collection<Permit> permits;
    private final String name;

    public Policy(String name, Permit... permits) {
        this.name = name;
        this.permits = Arrays.asList(permits);
    }

    public Collection<Permit> getPermits() {
        return permits;
    }

    public String getName() {
        return name;
    }
}