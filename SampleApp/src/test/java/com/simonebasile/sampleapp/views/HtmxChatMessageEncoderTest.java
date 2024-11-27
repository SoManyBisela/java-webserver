package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.views.chat.HtmxChatMessageEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HtmxChatMessageEncoderTest {

    private HtmxChatMessageEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new HtmxChatMessageEncoder();
    }

    @Test
    void testEncodeConnected() {
        ChatProtoMessage message = ChatProtoMessage.connected();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("Begin chat"));
    }

    @Test
    void testEncodeWaitForChat() {
        ChatProtoMessage message = ChatProtoMessage.waitingForChat();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("Waiting for available employee"));
    }

    @Test
    void testEncodeAlreadyConnected() {
        ChatProtoMessage message = ChatProtoMessage.alreadyConnected();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("You are already connected to the chat system from another window"));
    }

    @Test
    void testEncodeChatConnected() {
        ChatProtoMessage message = ChatProtoMessage.chatConnected("testUser");
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("testUser"));
    }

    @Test
    void testEncodeChatDisconnected() {
        ChatProtoMessage message = ChatProtoMessage.chatDisconnected();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.toLowerCase().contains("chat disconnected"));
    }

    @Test
    void testEncodeMessageReceived() {
        ChatProtoMessage message = ChatProtoMessage.messageReceived("Hello");
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("Hello"));
    }

    @Test
    void testEncodeMessageSent() {
        ChatProtoMessage message = ChatProtoMessage.messageSent("Hello");
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("Hello"));
    }

    @Test
    void testEncodeChatAvailable() {
        ChatProtoMessage message = ChatProtoMessage.chatAvailable();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("There are users waiting to chat"));
    }

    @Test
    void testEncodeNoChatAvailable() {
        ChatProtoMessage message = ChatProtoMessage.noChatAvailable();
        byte[] result = encoder.encode(message);
        String resultString = new String(result, StandardCharsets.UTF_8);
        assertTrue(resultString.contains("There are no chat requests"));
    }

}