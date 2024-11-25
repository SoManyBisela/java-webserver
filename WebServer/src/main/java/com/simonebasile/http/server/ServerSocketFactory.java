package com.simonebasile.http.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This interface is used to create server sockets.
 * It can be overridden by application developers to configure how server sockets are created by the web server.
 */
public interface ServerSocketFactory {
    ServerSocket createSocket() throws IOException;
}
