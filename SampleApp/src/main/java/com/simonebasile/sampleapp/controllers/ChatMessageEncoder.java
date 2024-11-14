package com.simonebasile.sampleapp.controllers;

import com.simonebasile.sampleapp.dto.ChatProtoMessage;

public interface ChatMessageEncoder {
    byte[] encode(ChatProtoMessage message);
}
