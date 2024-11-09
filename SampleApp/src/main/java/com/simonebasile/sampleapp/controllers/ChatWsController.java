package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.NewWsHandler;
import com.simonebasile.http.WebsocketMessage;
import com.simonebasile.http.WebsocketWriter;
import com.simonebasile.http.WebsocketWriterImpl;
import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.json.JsonMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.chat.*;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.simonebasile.sampleapp.views.html.HtmlElement.div;
//TODO increase logging
//TODO maybe move message rendering outside
@Slf4j
public class ChatWsController implements NewWsHandler<ChatWsController.WsState> {
    private final SessionService sessionService;
    private final UserService userService;
    private final ConcurrentHashMap<String, ConnectedUser> connectedUsers;
    private final ConcurrentLinkedQueue<String> waitingToChat;

    private static class ConnectedUser implements WebsocketWriter {
        private User user;
        private String connectedTo;
        private AtomicBoolean requestChat;
        private WebsocketWriter writer;

        public ConnectedUser(User user, WebsocketWriter writer) {
            this.user = user;
            this.writer = writer;
            this.connectedTo = null;
            this.requestChat = new AtomicBoolean(false);
        }

        @Override
        public void sendText(String s) throws IOException {
            writer.sendText(s);
        }

        private static HtmlElement obswap(String id, HtmlElement el) {
            return obswap(id, "true", el);
        }

        private static HtmlElement obswap(String id, String swap, HtmlElement el) {
            return el.attr( "id", id).hxSwapOob(swap);
        }

        public void sendMsg(ChatProtoMessage msg) throws IOException {
            final IHtmlElement s = switch (msg.getType()) {
                case CONNECTED -> obswap("chat-container", div().content(
                        new WantToChatElement()
                ));
                case WAIT_FOR_CHAT -> obswap("chat-container", div().content(
                        div().text("Waiting for connection"),
                        new StopWaitingElement()
                ));
                case ALREADY_CONNECTED -> obswap("chat-section", new AlreadyConnectedSection());
                case CHAT_CONNECTED -> obswap("chat-container", div().content(
                        div().attr("id", "messages", "class", "message-container"),
                        div().attr("id", "chat-inputs-container").content(
                                new SendMessageElement(),
                                new EndChatElement()
                        )
                ));
                case CHAT_DISCONNECTED -> obswap("chat-inputs-container", div().content(
                        div().text("Chat disconnected"),
                        new RestartChatElement()
                ));
                case MESSAGE_RECEIVED ->
                        obswap("messages", "beforeend", div().content(
                                div().attr("class", "message-row received").content(
                                        div().attr("class", "message").text(msg.getMessage()))));
                case MESSAGE_SENT ->
                        new ElementGroup(
                                obswap("messages", "beforeend", div().content(
                                        div().attr("class", "message-row sent").content(
                                                div().attr("class", "message").text(msg.getMessage())))),
                                new SendMessageElement().focusOnLoad()
                        );
                case CHAT_AVAILABLE -> obswap("chat-container", div().content(
                        new ElementGroup(
                                div().text("There are users waiting to chat"),
                                new AcceptChatElement()
                        )
                ));
                case NO_CHAT_AVAILABLE -> obswap("chat-container", div().content(
                        div().text("There are no chat requests")
                ));
                default -> throw new IllegalStateException("Unexpected type: " + msg.getType());
            };
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            s.write(out);
            sendTextBytes(out.toByteArray());
        }

        @Override
        public void sendTextBytes(byte[] s) throws IOException {
            writer.sendTextBytes(s);
        }

        @Override
        public void sendBytes(String s) throws IOException {
            writer.sendBytes(s);
        }

        @Override
        public void sendClose() {
            writer.sendClose();
        }
    }
    public ChatWsController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.waitingToChat = new ConcurrentLinkedQueue<>();
        this.connectedUsers = new ConcurrentHashMap<>();
    }

    public static class WsState{
        private User user;
        private ConnectedUser writer;
    }

    @Override
    public WsState newContext() {
        return new WsState();
    }

    @Override
    public HandshakeResult onServiceHandshake(String[] availableService, WsState ctx) {
        SessionData sessionData = sessionService.currentSession();
        if(sessionData == null) {
            return HandshakeResult.refuse("Connection refused");
        }
        ctx.user = userService.getUser(sessionData.getUsername());
        //if(dispatcher.containsKey(ctx.user.getUsername())) {
        //    return HandshakeResult.refuse("User already connected");
        //}
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
        ctx.writer = new ConnectedUser(ctx.user, websocketWriter);
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
        //TODO handle error
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
