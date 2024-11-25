package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.handlers.WebsocketHandler;
import com.simonebasile.http.handlers.WebsocketMessage;
import com.simonebasile.http.handlers.WebsocketWriter;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.ChatMessageEncoder;
import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.json.JsonMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.chat.HtmxChatMessageEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller for the chat websocket
 * Each user can be connected only once to the chat websocket.
 * The controller manages the chat between users.
 * Two type of users can connect to the chat:
 * - Users: they can request a chat with an employee
 * - Employees: they can accept a chat request from a user
 *
 * A typical chat flow is:
 * 1. User sends a WANT_TO_CHAT message
 * 2. Employee sends an ACCEPT_CHAT message
 * 3. Users and Employees exchange messages by sending SEND_MESSAGE messages
 * 4. Users and Employees can end the chat by sending END_CHAT messages
 * Additionally, a user can send a STOP_WAITING message to cancel the chat request
 *
 * Connected users are stored in the connectedUsers ConcurrentHashMap.
 * Users waiting to chat are stored in the waitingToChat ConcurrentLinkedQueue.
 */
@Slf4j
public class ChatWsController implements WebsocketHandler<ChatWsController.WsState, ApplicationRequestContext> {
    private final ConcurrentHashMap<String, ConnectedUser> connectedUsers;
    private final ConcurrentLinkedQueue<String> waitingToChat;

    /**
     * Represents a connected user
     *
     * Holds the user connection state
     * user: the user dto of the connected user
     * connectedTo: the username of the user the current user is connected to
     * requestChat: a flag to indicate if the user requested a chat, only ever true for users
     * writer: the writer to send messages to the user
     * encoder: the encoder to encode messages to send to the client
     */
    @Getter
    static class ConnectedUser implements WebsocketWriter {
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
        public void sendClose() throws IOException{
            writer.sendClose();
        }
    }

    public ChatWsController() {
        this.waitingToChat = new ConcurrentLinkedQueue<>();
        this.connectedUsers = new ConcurrentHashMap<>();
    }

    /**
     * Represents the state of the websocket connection
     * user: the user that is connected to the websocket
     * writer: the state of the connected user. It is null until the handshake is complete. Also used to send messages to the user
     */
    @Getter
    public static class WsState{
        private final User user;
        private ConnectedUser writer;

        public WsState(User user) {
            this.user = user;
        }
    }

    /**
     * Initializes the WsState context for the websocket connection
     */
    @Override
    public WsState newContext(ApplicationRequestContext requestContext) {
        return new WsState(requestContext.getLoggedUser());
    }

    /**
     * Handles the handshake request
     * The user must be logged in to connect to the chat
     * The user can connect only once
     * The user can connect only to the chat protocol
     */
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


    /**
     * Handles the handshake completion
     * The user is added to the connectedUsers map
     */
    @Override
    public void onHandshakeComplete(WebsocketWriter websocketWriter, WsState ctx) {
        log.debug("Handshake completed for user: {}", ctx.user.getUsername());
        final String username = ctx.user.getUsername();
        ctx.writer = new ConnectedUser(ctx.user, websocketWriter, new HtmxChatMessageEncoder());
        if(connectedUsers.putIfAbsent(username, ctx.writer) != null) {
            try {
                ctx.writer.sendMsg(ChatProtoMessage.alreadyConnected());
                ctx.writer.sendClose();
                return;
            } catch (IOException e) {
                log.error("Error while sending message: {}", e.getMessage(), e);
            }
        }
        newChat(ctx.writer);
    }

    /**
     * Handles the message received by the websocket
     * The message is parsed and the appropriate action is taken
     * The user role is checked to ensure that the user can send the message
     * The message is then processed
     */
    @Override
    public void onMessage(WebsocketMessage msg, WsState ctx) {
        if(msg.type == WebsocketMessage.MsgType.BINARY || msg.data.length > 1) {
            log.warn("Received binary message. Quitting");
            try {
                ctx.writer.sendClose();
            } catch (IOException e) {
                log.error("Error while sending close message: {}", e.getMessage(), e);
            }
            return;
        }
        ChatProtoMessage message = JsonMapper.parse(msg.data[0], ChatProtoMessage.class);
        log.debug("Received message {} from user {}", message, ctx.user.getUsername());
        final User user = ctx.user;
        if(!message.getType().canBeSentBy(user)) {
            log.warn("User sent invalid message. User role: {}, Message type: {}",
                    user.getRole(), message.getType());
            try {
                ctx.writer.sendClose();
            } catch (IOException e) {
                log.error("Error while sending close message: {}", e.getMessage(), e);
            }
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

    /**
     * Sends the user a message to initialize the chat
     */
    private void newChat(ConnectedUser connectedUser) {
        try {
            if(connectedUser.user.getRole() == Role.user) {
                log.info("User {} chat initialized", connectedUser.user.getUsername());
                connectedUser.sendMsg(ChatProtoMessage.connected());
            } else {
                if(waitingToChat.isEmpty()) {
                    log.info("User {} notified with no available chat", connectedUser.user.getUsername());
                    connectedUser.sendMsg(ChatProtoMessage.noChatAvailable());
                } else {
                    log.info("User {} notified with chat available", connectedUser.user.getUsername());
                    connectedUser.sendMsg(ChatProtoMessage.chatAvailable());
                }
            }
        } catch (IOException e) {
            log.error("Error while sending message: {}", e.getMessage(), e);
        }
    }

    /**
     * Ends the chat between the two connected users
     * The users are notified that the chat has ended
     */
    private void endChat(ConnectedUser disconnectingUser) {
        log.debug("Ending chat between {} and {}", disconnectingUser.user.getUsername(), disconnectingUser.connectedTo);
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

    /**
     * Connects the user to the first user in the chat queue
     * The user is removed from the chat queue
     */
    private void connectToAvailable(ConnectedUser acceptingUser) {
        final String queuedUsername = waitingToChat.poll();
        ConnectedUser connectedUser;
        if(queuedUsername == null ||
                (connectedUser = connectedUsers.get(queuedUsername)) == null ||
                !connectedUser.requestChat.compareAndSet(true, false)) {
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

    /**
     * Sends a message to the connected user
     * The message is sent to the user the current user is connected to
     */
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

    /**
     * Removes the user from the chat queue
     * The user is removed from the waitingToChat queue
     * Employees that are not connected to a user are notified with the current state of the chat queue
     */
    private void removeChatFromQueue(ConnectedUser usr) {
        if(usr.requestChat.compareAndSet(true, false)) {
            waitingToChat.remove(usr.user.getUsername());
            ChatProtoMessage msg = waitingToChat.isEmpty() ?
                    ChatProtoMessage.noChatAvailable() : ChatProtoMessage.chatAvailable();
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

    /**
     * Adds the user to the chat queue
     * The user is added to the waitingToChat queue
     * Employees that are not connected to a user are notified that a user is waiting to chat
     */
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

    /**
     * Handles the connection close
     * The user is removed from the connectedUsers map
     * The user is removed from the waitingToChat queue
     * The chat is ended if the user is connected
     */
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
