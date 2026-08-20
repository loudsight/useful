package com.loudsight.useful.helper.web;

import com.loudsight.useful.helper.ExceptionHelper;

import java.net.ServerSocket;

public final class WebHelper {

    private WebHelper() {
    }

    public static int getAvailablePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (Exception e) {
            ExceptionHelper.uncheckedThrow(e);
            throw new IllegalStateException("This code should be unreachable", e);
        }
    }
}
