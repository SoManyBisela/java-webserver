package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.WebsocketHandler;
import com.simonebasile.http.WebsocketMessage;
import com.simonebasile.http.WebsocketWriter;
import com.simonebasile.http.WebsocketWriterImpl;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.json.JsonMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.chat.HtmxChatMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

//TODO increase logging
@Slf4j
public class ChatWsController implements WebsocketHandler<ChatWsController.WsState, ApplicationRequestContext> {
    private final ConcurrentHashMap<String, ConnectedUser> connectedUsers;
    private final ConcurrentLinkedQueue<String> waitingToChat;

    private static class ConnectedUser implements WebsocketWriter {
        private User user;
        private String connectedTo;
        private AtomicBoolean requestChat;
        private WebsocketWriter writer;
        private ChatMessageEncoder encoder;

        public ConnectedUser(User user, WebsocketWriter writer, ChatMessageEncoder encoder) {
            this.user = user;
            this.writer = writer;
            this.encoder = encoder;
            this.connectedTo = null;
            this.requestChat = new AtomicBoolean(false);

        }

        @Override
        public void sendText(String s) throws IOException {
            writer.sendText(s);
        }

        public void sendMsg(ChatProtoMessage msg) throws IOException {
            sendTextBytes(encoder.encode(msg));
        }

        @Override
        public void sendTextBytes(byte[] s) throws IOException {
            writer.sendTextBytes(s);
        }

        @Override
        public void sendBytes(byte[] bytes) throws IOException {
            writer.sendBytes(bytes);
        }

        @Override
        public void sendClose() {
            writer.sendClose();
        }
    }
    public ChatWsController() {
        this.waitingToChat = new ConcurrentLinkedQueue<>();
        this.connectedUsers = new ConcurrentHashMap<>();
    }

    public static class WsState{
        private final User user;
        private ConnectedUser writer;

        public WsState(User user) {
            this.user = user;
        }
    }

    @Override
    public WsState newContext(ApplicationRequestContext requestContext) {
        return new WsState(requestContext.getLoggedUser());
    }

    @Override
    public HandshakeResult onServiceHandshake(String[] availableService, WsState ctx) {
        if(ctx.user == null) {
            return HandshakeResult.refuse("Connection refused");
        }
        if(connectedUsers.containsKey(ctx.user.getUsername())) {
            return HandshakeResult.refuse("User already connected");
        }
        if(availableService.length == 0) {
            return HandshakeResult.accept("chat");
        }
        for (String s : availableService) {
            if(s.equals("chat")) {
                return HandshakeResult.accept(s);
            }
        }
        return HandshakeResult.refuse("Invalid protocol");
    }

    @Override
    public void onHandshakeComplete(WebsocketWriterImpl websocketWriter, WsState ctx) {
        final String username = ctx.user.getUsername();
        ctx.writer = new ConnectedUser(ctx.user, websocketWriter, new HtmxChatMessageEncoder());
        if(connectedUsers.putIfAbsent(username, ctx.writer) != null) {
            try {
                ctx.writer.sendMsg(ChatProtoMessage.alreadyConnected());
            } catch (IOException e) {
                log.error("Error while sending message: {}", e.getMessage(), e);
            }
            ctx.writer.sendClose();
        }
        newChat(ctx.writer);
    }

    @Override
    public void onMessage(WebsocketMessage msg, WsState ctx) {
        if(msg.type == WebsocketMessage.MsgType.BINARY || msg.data.length > 1) {
            log.warn("Received binary message. Quitting");
            ctx.writer.sendClose();
            return;
        }
        ChatProtoMessage message = JsonMapper.parse(msg.data[0], ChatProtoMessage.class);
        log.debug("Received message: {}", message);
        final User user = ctx.user;
        if(!message.getType().canBeSentBy(user)) {
            log.warn("User sent invalid message. User role: {}, Message type: {}", user.getRole(), message.getType());
            ctx.writer.sendClose();
            return;
        }
        switch (message.getType()) {
            case WANT_TO_CHAT -> addToChatQueue(ctx.writer);
            case STOP_WAITING -> removeChatFromQueue(ctx.writer);
            case ACCEPT_CHAT -> connectToAvailable(ctx.writer);
            case SEND_MESSAGE -> sendMessage(ctx.writer, message.getMessage());
            case END_CHAT -> endChat(ctx.writer);
            case NEW_CHAT -> newChat(ctx.writer);
        }
    }

    private void newChat(ConnectedUser connectedUser) {
        try {
            if(connectedUser.user.getRole() == Role.user) {
                connectedUser.sendMsg(ChatProtoMessage.connected());
            } else {
                if(waitingToChat.isEmpty()) {
                    connectedUser.sendMsg(ChatProtoMessage.noChatAvailable());
                } else {
                    connectedUser.sendMsg(ChatProtoMessage.chatAvailable());
                }
            }
        } catch (IOException e) {
            log.error("Error while sending message: {}", e.getMessage(), e);
        }
    }

    private void endChat(ConnectedUser disconnectingUser) {
        final ConnectedUser connectedUser = connectedUsers.get(disconnectingUser.connectedTo);
        disconnectingUser.connectedTo = null;
        if(connectedUser != null) {
            connectedUser.connectedTo = null;
            try {
                connectedUser.sendMsg(ChatProtoMessage.chatDisconnected());
                disconnectingUser.sendMsg(ChatProtoMessage.chatDisconnected());
            } catch (IOException e) {
                log.error("Error sending disconnection message. {}", e.getMessage(), e);
            }
        }
    }

    private void connectToAvailable(ConnectedUser acceptingUser) {
        final String queuedUsername = waitingToChat.poll();
        final ConnectedUser connectedUser = connectedUsers.get(queuedUsername);
        if(connectedUser == null || !connectedUser.requestChat.compareAndSet(true, false)) {
            try {
                acceptingUser.sendMsg(ChatProtoMessage.noChatAvailable());
            }catch (IOException e) {
                log.error("Error sending no chat available notification{}", e.getMessage(), e);
            }
            return;
        }
        acceptingUser.connectedTo = queuedUsername;
        connectedUser.connectedTo = acceptingUser.user.getUsername();
        try {
            acceptingUser.sendMsg(ChatProtoMessage.chatConnected(acceptingUser.connectedTo));
            connectedUser.sendMsg(ChatProtoMessage.chatConnected(connectedUser.connectedTo));
        } catch (Exception e) {
            log.error("Error sending connected messages {}", e.getMessage(), e);
        }
    }

    private void sendMessage(ConnectedUser source, String message) {
        ConnectedUser target;
        try {
            if(source.connectedTo == null || // Sender is not connected
                    (target = connectedUsers.get(source.connectedTo))  == null || // Receiver is not connected
                    !source.user.getUsername().equals(target.connectedTo)) { // Receiver is not connected to the sender
                //The other user disconnected
                log.warn("Target user disconnected before receiving message");
                source.sendMsg(ChatProtoMessage.chatDisconnected());
                return;
            }
            target.sendMsg(ChatProtoMessage.messageReceived(message));
            source.sendMsg(ChatProtoMessage.messageSent(message));
        } catch (IOException e) {
            log.error("An exception occurred while sending a message: {}", e.getMessage(), e);
        }
    }

    private void removeChatFromQueue(ConnectedUser usr) {
        if(usr.requestChat.compareAndSet(true, false)) {
            waitingToChat.remove(usr.user.getUsername());
            ChatProtoMessage msg = waitingToChat.isEmpty() ? ChatProtoMessage.noChatAvailable() : ChatProtoMessage.chatAvailable();
            connectedUsers.values().forEach(v -> {
                if(v.user.getRole() == Role.employee && v.connectedTo == null) {
                    try {
                        v.sendMsg(msg);
                    } catch (Exception e) {
                        log.error("Error while sending message request queue size to {}: {}", v.user.getUsername(), e.getMessage(), e);
                    }
                }
            });
        } else {
            log.warn("Already removed chat from queue");
        }
        try {
            usr.sendMsg(ChatProtoMessage.connected());
        } catch (IOException e) {
            log.error("Errore nell'invio del messaggio all'utente: {}", e.getMessage(), e);
        }
    }

    private void addToChatQueue(ConnectedUser connected) {
        if(connected.requestChat.compareAndSet(false, true)) {
            waitingToChat.add(connected.user.getUsername());
        } else {
            log.warn("Multiple requests to chat received");
        }
        connectedUsers.values().forEach(v -> {
            if(v.user.getRole() == Role.employee && v.connectedTo == null) {
                try {
                    v.sendMsg(ChatProtoMessage.chatAvailable());
                } catch (Exception e) {
                    log.error("Error while sending message request queue size to {}: {}", v.user.getUsername(), e.getMessage(), e);
                }
            }
        });
        try {
            connected.sendMsg(ChatProtoMessage.waitingForChat());
        } catch (IOException e) {
            log.error("Errore nell'invio del messaggio all'utente: {}", e.getMessage(), e);
        }
    }


    @Override
    public void onClose(WsState ctx) {
        final ConnectedUser writer = ctx.writer;
        removeChatFromQueue(writer);
        if(writer.connectedTo != null) {
            endChat(writer);
        }
        connectedUsers.remove(ctx.user.getUsername());
    }
}
