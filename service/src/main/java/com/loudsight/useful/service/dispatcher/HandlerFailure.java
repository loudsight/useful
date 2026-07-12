package com.loudsight.useful.service.dispatcher;

import com.loudsight.meta.annotation.Introspect;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Wire-serializable failure reply. Published as the reply payload when a subscribed handler
 * throws, instead of silently dropping the reply (which previously left the caller blocked
 * until its own client-side timeout with no indication of what went wrong on either side).
 * <p>
 * {@code stackTrace} carries the server-side handler's full stack trace (as text, since it has
 * to survive wire serialization) so a failure logged on the caller's side is actionable on its
 * own - without it, tracking down what actually broke means correlating timestamps against the
 * server's own logs, which may have already rolled over or be on a different host entirely.
 */
@Introspect(clazz = HandlerFailure.class)
public record HandlerFailure(String message, String exceptionType, String stackTrace) {

    public static HandlerFailure of(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return new HandlerFailure(String.valueOf(t.getMessage()), t.getClass().getName(), sw.toString());
    }
}
