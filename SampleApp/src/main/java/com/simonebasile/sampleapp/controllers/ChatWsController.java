package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.NewWsHandler;
import com.simonebasile.http.WebsocketMessage;
import com.simonebasile.http.WebsocketWriter;
import com.simonebasile.http.WebsocketWriterImpl;
import com.simonebasile.sampleapp.chat.MessageDispatcher;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatWsController implements NewWsHandler<ChatWsController.WsState> {
    private final SessionService sessionService;
    private final UserService userService;
    private final MessageDispatcher<ConnectedUser> dispatcher;
    private final ConcurrentLinkedQueue<String> waitingToChat;

    public ChatWsController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.waitingToChat = new ConcurrentLinkedQueue<>();
        this.dispatcher = new MessageDispatcher<>();
    }


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
            return el.attr("hx-oob-swap", swap, "id", id);
        }

        public void sendMsg(ChatProtoMessage msg) throws IOException{
            final IHtmlElement s = switch (msg.type) {
                case CONNECTED -> obswap("chat-input-container", HtmlElement.div().content(
                        user.getRole() == Role.user ?
                                new WantToChatElement() :
                                new ElementGroup(
                                        HtmlElement.div().text("No chat requests"),
                                        new AcceptChatElement()
                                )
                ));
                case ALREADY_CONNECTED -> obswap("chat-section", new AlreadyConnectedSection());
                case CHAT_CONNECTED -> obswap("chat-container", HtmlElement.div().content(
                        HtmlElement.div().attr("id", "messages", "class", "message-container"),
                        HtmlElement.div().attr("id", "chat-inputs-container").content(
                                new SendMessageElement(),
                                new EndChatElement()
                        )
                ));
                case CHAT_DISCONNECTED -> obswap("chat-inputs-container", HtmlElement.div().text("Chat disconnected"));
                case MESSAGE_RECEIVED -> obswap("messages", "beforeend", HtmlElement.div().attr("class", "message received").text(msg.getMessage()));
                case MESSAGE_SENT -> obswap("messages", "beforeend", HtmlElement.div().attr("class", "message sent").text(msg.getMessage()));
                case CHAT_REQUESTED -> null;
                case NO_CHAT_AVAILABLE -> null;
                default -> throw new IllegalStateException("Unexpected type: " + msg.type);
            };
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            s.write(byteArrayOutputStream);
            sendText(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
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


    public static class WsState{
        private User user;
        private ConnectedUser writer;
    }

    public enum CPMType {
        //S to C
        CONNECTED,
        ALREADY_CONNECTED,
        CHAT_CONNECTED, //Username
        CHAT_DISCONNECTED,
        MESSAGE_RECEIVED, //Message
        MESSAGE_SENT, //Message

        //S to E
        CHAT_REQUESTED, //N of chats
        NO_CHAT_AVAILABLE,

        //U to S
        WANT_TO_CHAT(Role.user),

        //E to S
        ACCEPT_CHAT(Role.employee),

        //C to S
        SEND_MESSAGE(Role.user, Role.employee), //Message
        END_CHAT(Role.employee, Role.employee),;

        private final Role[] canSend;

        CPMType(Role... canSend) {
            this.canSend = canSend;
        }

        boolean canBeSentBy(User u) {
            for (Role role : this.canSend) {
                if(u.getRole() == role) return true;
            }
            return false;
        }
    }

    @Setter
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatProtoMessage {
        private CPMType type;
        private Integer nval;
        private String sval;

        public String getMessage() {
            if(this.type == CPMType.SEND_MESSAGE || this.type == CPMType.MESSAGE_SENT) {
                return sval;
            }
            throw new IllegalStateException("message type doesn't support message");
        }

        public static ChatProtoMessage alreadyConnected() {
            return new ChatProtoMessage(CPMType.ALREADY_CONNECTED, null, null);
        }

        public static ChatProtoMessage chatRequested(int nchat) {
            return new ChatProtoMessage(CPMType.CHAT_REQUESTED, nchat, null);
        }

        public static ChatProtoMessage chatConnected(String username) {
            return new ChatProtoMessage(CPMType.CHAT_CONNECTED, null, username);
        }

        public static ChatProtoMessage chatDisconnected() {
            return new ChatProtoMessage(CPMType.CHAT_DISCONNECTED, null, null);
        }

        public static ChatProtoMessage messageReceived(String message) {
            return new ChatProtoMessage(CPMType.MESSAGE_RECEIVED, null, message);
        }

        public static ChatProtoMessage noChatAvailable() {
            return new ChatProtoMessage(CPMType.NO_CHAT_AVAILABLE, null, null);
        }


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
        if(dispatcher.containsKey(ctx.user.getUsername())) {
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
        ctx.writer = new ConnectedUser(ctx.user, websocketWriter);
        if(!dispatcher.registerClient(username, ctx.writer)) {
            try {
                ctx.writer.sendMsg(ChatProtoMessage.alreadyConnected());
            } catch (IOException e) {
                log.error("Error while sending message: {}", e.getMessage(), e);
            }
            ctx.writer.sendClose();
        }
    }

    @Override
    public void onMessage(WebsocketMessage msg, WsState ctx) {
        if(msg.type == WebsocketMessage.MsgType.BINARY || msg.data.length > 1) {
            log.warn("Received binary message. Quitting");
            ctx.writer.sendClose();
            return;
        }
        ChatProtoMessage message = JsonMapper.parse(msg.data[0], ChatProtoMessage.class);
        log.info("Received message: {}", message);
        final User user = ctx.user;
        if(!message.type.canBeSentBy(user)) {
            log.warn("User sent invalid message. User role: {}, Message type: {}", user.getRole(), message.type);
            ctx.writer.sendClose();
            return;
        }
        switch (message.type) {
            case WANT_TO_CHAT -> addToChatQueue(user.getUsername(), ctx.writer);
            case ACCEPT_CHAT -> connectToAvailable(ctx.writer);
            case SEND_MESSAGE -> sendMessage(ctx.writer, message.getMessage());
            case END_CHAT -> endChat(ctx.writer);
        }
    }

    private void endChat(ConnectedUser disconnectingUser) {
        disconnectingUser.connectedTo = null;
        final ConnectedUser connectedUser = dispatcher.get(disconnectingUser.connectedTo);
        if(connectedUser != null) {
            connectedUser.connectedTo = null;
            try {
                connectedUser.sendMsg(ChatProtoMessage.chatDisconnected());
            } catch (IOException e) {
                log.error("Error sending disconnection message. {}", e.getMessage(), e);
            }
        }
    }

    private void connectToAvailable(ConnectedUser acceptingUser) {
        final String queuedUsername = waitingToChat.poll();
        final ConnectedUser connectedUser = dispatcher.get(queuedUsername);
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
                    (target = dispatcher.get(source.connectedTo))  == null || // Receiver is not connected
                    !source.user.getUsername().equals(target.connectedTo)) { // Receiver is not connected to the sender
                //The other user disconnected
                log.warn("Target user disconnected before receiving message");
                source.sendMsg(ChatProtoMessage.chatDisconnected());
                return;
            }
            target.sendMsg(ChatProtoMessage.messageReceived(message));
        } catch (IOException e) {
            log.error("An exception occurred while sending a message: {}", e.getMessage(), e);
        }
    }

    private void addToChatQueue(String username, ConnectedUser usr) {
        if(usr.requestChat.compareAndSet(false, true)) {
            waitingToChat.add(username);
        } else {
            log.warn("Multiple requests to chat received");
        }
        int size = waitingToChat.size();
        dispatcher.values().forEach(v -> {
            if(v.user.getRole() == Role.employee) {
                try {
                    v.sendMsg(ChatProtoMessage.chatRequested(size));
                } catch (Exception e) {
                    log.error("Error while sending message request queue size to {}: {}", v.user.getUsername(), e.getMessage(), e);
                }
            }
        });
    }


    @Override
    public void onClose(WsState ctx) {
        dispatcher.remove(ctx.user.getUsername());
    }
}
