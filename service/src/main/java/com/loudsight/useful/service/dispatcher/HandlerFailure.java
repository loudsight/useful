package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

/**
 * Wire-serializable failure reply. Published as the reply payload when a subscribed handler
 * throws, instead of silently dropping the reply (which previously left the caller blocked
 * until its own client-side timeout with no indication of what went wrong on either side).
 */
@Introspect(clazz = HandlerFailure.class)
public record HandlerFailure(String message, String exceptionType) {
}
