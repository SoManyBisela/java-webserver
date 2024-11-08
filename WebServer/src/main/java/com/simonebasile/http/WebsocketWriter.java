package com.simonebasile.http;

import java.io.IOException;

public interface WebsocketWriter {
    void sendText(String s) throws IOException;

    void sendBytes(String s) throws IOException;

    void sendClose();
}
