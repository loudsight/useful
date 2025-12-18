package com.loudsight.useful.helper;

import java.net.ServerSocket;

public class WebHelper {

    public static int getAvailablePort() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        serverSocket.close();
        return port;
    }
}
