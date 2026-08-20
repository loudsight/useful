package com.loudsight.useful.helper;

import java.io.IOException;
import java.net.ServerSocket;

public final class WebHelper {

    private WebHelper() {
    }

    public static int getAvailablePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
