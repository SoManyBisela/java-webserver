package com.simonebasile.http.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class DefaultServerSocketFactory implements ServerSocketFactory{

    private final int port;
    private final int backlog;
    private final InetAddress address;

    public DefaultServerSocketFactory(int port, int backlog, InetAddress address) {
        this.port = port;
        this.backlog = backlog;
        this.address = address;
    }

    @Override
    public ServerSocket createSocket() throws IOException {
        return new ServerSocket(port, backlog, address);
    }

}
