package com.loudsight.useful.service.dispatcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnvelopeTest {
    private static final String FAKE_TOKEN = "fake-session-token-000";
    private static final String FAKE_PAYLOAD = "fake-payload-value-000";

    @Test
    public void toStringOmitsSessionTokenAndPayload() {
        var text = new Envelope(FAKE_PAYLOAD, "fake-caller", FAKE_TOKEN).toString();

        assertFalse(text.contains(FAKE_TOKEN), () -> "session token was printed: " + text);
        assertFalse(text.contains(FAKE_PAYLOAD), () -> "payload was printed: " + text);
        assertTrue(text.contains("payloadType=" + String.class.getName()), () -> "payload type missing: " + text);
        assertTrue(text.contains("caller=fake-caller"), () -> "caller missing: " + text);
        assertTrue(text.contains("sessionToken=<redacted>"), () -> "token marker missing: " + text);
    }

    @Test
    public void toStringShowsAbsentSessionTokenAsNull() {
        var text = new Envelope(FAKE_PAYLOAD).toString();

        assertFalse(text.contains(FAKE_PAYLOAD), () -> "payload was printed: " + text);
        assertTrue(text.contains("sessionToken=null"), () -> "absent token not shown as null: " + text);
    }

    @Test
    public void toStringOfNestedEnvelopeOmitsInnerValues() {
        var inner = new Envelope(FAKE_PAYLOAD, "fake-caller", FAKE_TOKEN);

        var text = String.valueOf(new Envelope(inner));

        assertFalse(text.contains(FAKE_TOKEN), () -> "session token was printed: " + text);
        assertFalse(text.contains(FAKE_PAYLOAD), () -> "payload was printed: " + text);
    }
}
