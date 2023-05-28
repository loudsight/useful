package com.loudsight.utilities.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

import java.util.Objects;

@Introspect(clazz = Address.class)
public class Address {
    public static Address NO_REPLY = new Address("no-reply", "no-reply");

    public static Address WILDCARD_ADDRESS = new Address("*", "*");
    private final String scope;
    private final String topic;
    public Address(String scope, String topic) {
        this.scope = scope;
        this.topic = topic;
    }

    public String getScope() {
        return scope;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Address other) {
            return Objects.equals(other.scope, scope) &&
                    Objects.equals(other.topic, topic);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0x70 ^ scope.hashCode() ^ topic.hashCode();
    }
}