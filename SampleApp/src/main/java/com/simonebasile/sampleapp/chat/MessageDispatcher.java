package com.simonebasile.sampleapp.chat;

import com.simonebasile.http.WebsocketWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher {
    private final ConcurrentHashMap<String, WebsocketWriter> clients;

    public MessageDispatcher() {
        clients = new ConcurrentHashMap<>();
    }


    public void sendMessage(String clientId, String message) {
        try {
            clients.get(clientId).sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerClient(String clientId, WebsocketWriter writer) {
        clients.put(clientId, writer);
    }

    public void markDisconnected(String clientId) {
        clients.remove(clientId);
    }

    public void broadcast(String message) {
        clients.forEach((k, w)->{
            try {
                w.sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String registeredList() {
        return clients.reduceKeys(1000, (a, b) -> a + ":" + b);
    }
}
