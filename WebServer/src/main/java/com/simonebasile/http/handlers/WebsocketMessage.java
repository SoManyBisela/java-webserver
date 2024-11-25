package com.simonebasile.http.handlers;

import java.util.Objects;

/**
 * This class represents a message sent through a websocket.
 * data is an array of byte arrays, each containing a fragment of the message.
 */
public class WebsocketMessage {
    public enum MsgType {
        TEXT,
        BINARY
    }
    public final byte[][] data;
    public final MsgType type;

    public WebsocketMessage(byte[][] data, MsgType type) {
        this.data = Objects.requireNonNull(data);
        this.type = Objects.requireNonNull(type);
    }
}
