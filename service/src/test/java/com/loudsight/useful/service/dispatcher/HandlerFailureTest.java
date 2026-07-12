package com.loudsight.useful.service.dispatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandlerFailureTest {

    @Test
    public void ofCapturesMessageTypeAndStackTrace() {
        Throwable thrown;
        try {
            throw new IllegalStateException("boom");
        } catch (IllegalStateException e) {
            thrown = e;
        }

        var failure = HandlerFailure.of(thrown);

        assertEquals("boom", failure.message());
        assertEquals(IllegalStateException.class.getName(), failure.exceptionType());
        assertTrue(failure.stackTrace().contains(IllegalStateException.class.getName()),
                "stack trace text should name the exception type: " + failure.stackTrace());
        assertTrue(failure.stackTrace().contains("HandlerFailureTest"),
                "stack trace text should include the throw site: " + failure.stackTrace());
    }

    @Test
    public void ofHandlesNullMessage() {
        var failure = HandlerFailure.of(new NullPointerException());

        assertEquals("null", failure.message());
        assertEquals(NullPointerException.class.getName(), failure.exceptionType());
        assertTrue(failure.stackTrace().contains(NullPointerException.class.getName()));
    }
}
