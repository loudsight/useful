package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

@Introspect(clazz = Address.class)
public record Address(String scope, String topic) {
}