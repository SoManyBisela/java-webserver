package com.simonebasile.sampleapp.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.simonebasile.sampleapp.model.SessionData;

public class SessionRepository {
    private final MongoCollection<SessionData> sessionCollection;

    public SessionRepository(MongoCollection<SessionData> sessionCollection) {
        this.sessionCollection = sessionCollection;
    }

    public SessionData getSession(String sessionId) {
        return sessionCollection.find(Filters.eq("_id", sessionId)).first();
    }

    public void createSession(SessionData sessionData) {
        sessionCollection.insertOne(sessionData);
    }

    public SessionData update(SessionData sessionData) {
        sessionCollection.updateOne(Filters.eq("_id", sessionData.getId()),
                Updates.set("username", sessionData.getUsername()));
        return sessionData;
    }
}
