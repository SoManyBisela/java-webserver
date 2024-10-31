package com.simonebasile.http;

import java.util.Objects;

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
