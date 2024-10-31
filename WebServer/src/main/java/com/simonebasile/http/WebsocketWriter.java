package com.simonebasile.http;

import com.simonebasile.http.unpub.WebSocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebsocketWriter {
    private final WebSocket websocket;

    public WebsocketWriter(WebSocket websocket) {
        this.websocket = websocket;
    }

    public void sendText(String s) throws IOException {
        final byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        //TODO bytes length test
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_TEXT, bytes);
    }

    public void sendBytes(String s) throws IOException {
        //TODO bytes length test
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_BIN, s.getBytes(StandardCharsets.UTF_8));
    }

    public void sendClose() {
        //TODO
    }
}
