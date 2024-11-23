package com.simonebasile.sampleapp.dto;

/**
 * Encodes chat messages.
 */
public interface ChatMessageEncoder {
    /**
     * Encodes a chat message as a byte array.
     * @param message the message
     * @return the encoded message
     */
    byte[] encode(ChatProtoMessage message);
}
