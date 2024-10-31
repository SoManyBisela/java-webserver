package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.NewWsHandler;
import com.simonebasile.http.WebsocketMessage;
import com.simonebasile.http.WebsocketWriter;
import com.simonebasile.sampleapp.chat.MessageDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class ChatWsController implements NewWsHandler<ChatWsController.WsState> {
    public static class WsState{
        private String protocol;
        private String id;
        private WebsocketWriter writer;
    }

    private final MessageDispatcher dispatcher;

    public ChatWsController(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public WsState newContext() {
        return new WsState();
    }

    @Override
    public HandshakeResult serviceHandshake(String[] availableService, WsState ctx) {
        if(availableService.length == 0) {
            ctx.protocol = "chattest";
            return HandshakeResult.accept("chattest");
        }
        for (String s : availableService) {
            if(s.equals("chattest")) {
                ctx.protocol = "chattest";
                return HandshakeResult.accept("chattest");
            }
        }
        return HandshakeResult.refuse("Invalid protocol");
    }

    @Override
    public void onHandshakeComplete(WebsocketWriter websocketWriter, WsState ctx) {
        ctx.writer = websocketWriter;
        final String id = UUID.randomUUID().toString();
        ctx.id = id;
        dispatcher.broadcast("connected;" + id);
        dispatcher.registerClient(id, websocketWriter);
        try {
            websocketWriter.sendText("assign;" + id);
            websocketWriter.sendText("usrlist;" + dispatcher.registeredList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(WebsocketMessage msg, WsState ctx) {
        if(msg.type == WebsocketMessage.MsgType.BINARY || msg.data.length > 1) {
            log.error("Received binary message. Quitting");
            ctx.writer.sendClose();
            return;
        }
        final String message = new String(msg.data[0], StandardCharsets.UTF_8);
        log.info("Received message: {}", message);
        final int i = message.indexOf(";");
        String to = message.substring(0, i);
        String data = message.substring(i + 1);
        final String id = ctx.id;
        String fullMessage = "msg;" + id + ";" + data;
        if("broadcast".equals(to)) {
            dispatcher.broadcast(fullMessage);
        } else {
            dispatcher.sendMessage(to, fullMessage);
        }

    }

    @Override
    public void onClose(WsState ctx) {
        final String id = ctx.id;
        dispatcher.markDisconnected(id);
        dispatcher.broadcast("disconnected;" + id);
    }
}
