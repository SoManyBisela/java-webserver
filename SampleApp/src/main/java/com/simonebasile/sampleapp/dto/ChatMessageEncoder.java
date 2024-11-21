package com.simonebasile.sampleapp.dto;

public interface ChatMessageEncoder {
    byte[] encode(ChatProtoMessage message);
}
