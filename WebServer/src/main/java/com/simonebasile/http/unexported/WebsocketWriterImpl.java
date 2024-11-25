package com.simonebasile.http.unexported;

import com.simonebasile.http.WebsocketWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of WebsocketWriter.
 */
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
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_TEXT, bytes);
    }

    @Override
    public void sendBytes(byte[] bytes) throws IOException {
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_BIN, bytes);
    }

    @Override
    public void sendClose() throws IOException{
        websocket.sendUnmaskedDataframe(WebSocket.WSDataFrame.FIN, WebSocket.WSDataFrame.OP_CLOSE, new byte[0]);
    }
}
