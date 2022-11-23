package com.loudsight.utilities.helper.web;

import com.loudsight.utilities.helper.ExceptionHelper;

import java.net.ServerSocket;

public class WebHelper {

    public static int getAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (Exception e) {
            ExceptionHelper.uncheckedThrow(e);
            throw new IllegalStateException("This code should be unreachable", e);
        }
    }
}
