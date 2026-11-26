package com.loudsight.useful.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.loudsight.useful.service.dispatcher.Envelope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListenerTest {
    private static final String FAKE_TOKEN = "fake-session-token-000";
    private static final String FAKE_PAYLOAD = "fake-payload-value-000";

    private final Logger logger = (Logger) LoggerFactory.getLogger(Listener.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    public void captureLogs() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    public void releaseLogs() {
        logger.detachAppender(appender);
        appender.stop();
        logger.setLevel(originalLevel);
    }

    @Test
    public void logsResultTypeButNotResultValue() {
        var listener = new Listener<Envelope>();
        var sent = new Envelope(FAKE_PAYLOAD, "fake-caller", FAKE_TOKEN);

        listener.accept(sent);
        assertSame(sent, listener.getResult(1, TimeUnit.SECONDS));

        String logs = capturedLogs();
        assertFalse(logs.contains(FAKE_TOKEN), () -> "session token was logged:\n" + logs);
        assertFalse(logs.contains(FAKE_PAYLOAD), () -> "payload was logged:\n" + logs);
        assertTrue(logs.contains("accept() called with result type: " + Envelope.class.getName()),
                () -> "result type missing from accept log:\n" + logs);
        assertTrue(logs.contains("returned successfully, result type: " + Envelope.class.getName()),
                () -> "result type missing from getResult log:\n" + logs);
    }

    @Test
    public void logsNullResult() {
        var listener = new Listener<Envelope>();

        listener.accept(null);
        listener.getResult(1, TimeUnit.SECONDS);

        String logs = capturedLogs();
        assertTrue(logs.contains("accept() called with result type: null"),
                () -> "null result missing from accept log:\n" + logs);
    }

    private String capturedLogs() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }
}
