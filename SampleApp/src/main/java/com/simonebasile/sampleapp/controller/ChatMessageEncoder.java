package com.simonebasile.sampleapp.controller;

import com.simonebasile.sampleapp.dto.ChatProtoMessage;

public interface ChatMessageEncoder {
    byte[] encode(ChatProtoMessage message);
}
