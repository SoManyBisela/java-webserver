package com.simonebasile.http;

import java.net.Socket;

public interface WebsocketConnectionHandler {
    void newConnection(HttpRequest<Void> req, Socket client, HttpInputStream his, HttpOutputStream hos);
}
