package com.simonebasile.sampleapp.chat;

import com.simonebasile.http.WebsocketWriter;
import com.simonebasile.http.WebsocketWriterImpl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher<T extends WebsocketWriter> extends ConcurrentHashMap<String, T> {

    public MessageDispatcher() {
        super();
    }


    public void sendMessage(String clientId, String message) {
        try {
            super.get(clientId).sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean registerClient(String clientId, T writer) {
        return super.putIfAbsent(clientId, writer) == null;
    }

    public void markDisconnected(String clientId) {
        super.remove(clientId);
    }

    public void broadcast(String message) {
        super.forEach((k, w)->{
            try {
                w.sendText(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String registeredList() {
        return super.reduceKeys(1000, (a, b) -> a + ":" + b);
    }

    public boolean hasClient(String username) {
        return super.containsKey(username);
    }
}
