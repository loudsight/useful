package com.loudsight.utilities.web.proxy;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UrlProxyRoutesTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(UrlProxyRoutes.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Level originalLevel;

    @BeforeEach
    public void captureLogs() {
        originalLevel = logger.getLevel();
        logger.setLevel(Level.TRACE);
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
    public void doesNotLogSensitiveHeaderValues() throws Exception {
        var headers = new HttpHeaders();
        headers.add("Authorization", "Bearer fake-authorization-000");
        headers.add("proxy-authorization", "Basic fake-proxy-000");
        headers.add("Cookie", "SESSION=fake-cookie-000");
        headers.add("COOKIE", "OTHER=fake-cookie-111");
        headers.add("Set-Cookie", "SESSION=fake-set-cookie-000");
        headers.add("X-Custom-Token", "fake-custom-000");

        String logs = logsFor(headers);

        for (String secret : List.of("fake-authorization-000", "fake-proxy-000", "fake-cookie-000",
                "fake-cookie-111", "fake-set-cookie-000", "fake-custom-000")) {
            assertFalse(logs.contains(secret), () -> secret + " was logged:\n" + logs);
        }
        assertTrue(logs.contains("Authorization=<redacted>"), () -> "header name missing:\n" + logs);
        assertTrue(logs.contains("Cookie=<redacted>"), () -> "header name missing:\n" + logs);
    }

    @Test
    public void logsValuesOfNonSensitiveHeaders() throws Exception {
        var headers = new HttpHeaders();
        headers.add("ACCEPT", "application/json");
        headers.add("Content-Type", "text/plain");

        String logs = logsFor(headers);

        assertTrue(logs.contains("ACCEPT=application/json"), () -> "accept value missing:\n" + logs);
        assertTrue(logs.contains("Content-Type=text/plain"), () -> "content type value missing:\n" + logs);
    }

    private String logsFor(HttpHeaders httpHeaders) throws Exception {
        var requestHeaders = mock(ServerRequest.Headers.class);
        when(requestHeaders.asHttpHeaders()).thenReturn(httpHeaders);
        var request = mock(ServerRequest.class);
        when(request.headers()).thenReturn(requestHeaders);
        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.path()).thenReturn("/fake");
        var response = mock(ServerResponse.class);

        var routes = new UrlProxyRoutes<Object, Object>(List.of());
        assertSame(response, routes.logAndExecute(ignored -> response).handle(request));

        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }
}
