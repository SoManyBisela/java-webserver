package com.simonebasile.sampleapp.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatProtoMessage {
    @Getter
    private CPMType type;
    private String sval;


    public String getMessage() {
        if (this.type == CPMType.SEND_MESSAGE ||
                this.type == CPMType.MESSAGE_SENT ||
                this.type == CPMType.MESSAGE_RECEIVED) {
            return sval;
        }
        throw new IllegalStateException("message type doesn't support message");
    }

    public static ChatProtoMessage connected() {
        return new ChatProtoMessage(CPMType.CONNECTED, null);
    }

    public static ChatProtoMessage alreadyConnected() {
        return new ChatProtoMessage(CPMType.ALREADY_CONNECTED, null);
    }

    public static ChatProtoMessage chatAvailable() {
        return new ChatProtoMessage(CPMType.CHAT_AVAILABLE, null);
    }

    public static ChatProtoMessage chatConnected(String username) {
        return new ChatProtoMessage(CPMType.CHAT_CONNECTED, username);
    }

    public static ChatProtoMessage chatDisconnected() {
        return new ChatProtoMessage(CPMType.CHAT_DISCONNECTED, null);
    }

    public static ChatProtoMessage messageReceived(String message) {
        return new ChatProtoMessage(CPMType.MESSAGE_RECEIVED, message);
    }

    public static ChatProtoMessage messageSent(String message) {
        return new ChatProtoMessage(CPMType.MESSAGE_SENT, message);
    }

    public static ChatProtoMessage noChatAvailable() {
        return new ChatProtoMessage(CPMType.NO_CHAT_AVAILABLE, null);
    }


}
