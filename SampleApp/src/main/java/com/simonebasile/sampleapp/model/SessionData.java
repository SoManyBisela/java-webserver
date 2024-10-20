package com.simonebasile.sampleapp.model;

public class SessionData {
    private String id;
    private String username;

    public SessionData() {
    }

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

    @Override
    public String toString() {
        return "SessionData{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}