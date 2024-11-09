package com.simonebasile.http;

import com.simonebasile.http.unpub.WebSocket;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebsocketWriterImpl implements WebsocketWriter {
    private final WebSocket websocket;

    public WebsocketWriterImpl(WebSocket websocket) {
        this.websocket = websocket;
    }

    @Override
    public void sendText(String s) throws IOException {
        sendTextBytes(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendTextBytes(byte[] bytes) throws IOException {
        //TODO bytes length test
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_TEXT, bytes);
    }

    @Override
    public void sendBytes(String s) throws IOException {
        //TODO bytes length test
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_BIN, s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void sendClose() {
        //TODO
    }
}
