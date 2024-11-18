package com.simonebasile.http;

import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketFactory {
    ServerSocket createSocket() throws IOException;
}
