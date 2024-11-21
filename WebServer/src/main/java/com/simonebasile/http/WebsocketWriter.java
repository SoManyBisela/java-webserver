package com.simonebasile.http;

import java.io.IOException;

public interface WebsocketWriter {
    void sendText(String s) throws IOException;

    void sendTextBytes(byte[] bytes) throws IOException;

    void sendBytes(byte[] bytes) throws IOException;

    void sendClose() throws IOException;
}
