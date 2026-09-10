package com.loudsight.useful.helper.web;

import com.loudsight.useful.helper.ExceptionHelper;

import java.net.ServerSocket;

public final class WebHelper {

    private WebHelper() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UNENCRYPTED_SERVER_SOCKET",
            justification = "The socket is bound to port 0 purely to discover a free ephemeral "
                    + "port and is closed immediately; it never accepts a connection or carries "
                    + "data, so transport encryption does not apply.")
    public static int getAvailablePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (Exception e) {
            ExceptionHelper.uncheckedThrow(e);
            throw new IllegalStateException("This code should be unreachable", e);
        }
    }
}
