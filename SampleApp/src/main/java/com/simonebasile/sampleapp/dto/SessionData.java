package com.simonebasile.sampleapp.dto;

public class SessionData {
    private String id;
    private String username;

    public SessionData(String sessionId) {
        this.id = sessionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
