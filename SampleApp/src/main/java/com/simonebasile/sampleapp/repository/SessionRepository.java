package com.simonebasile.sampleapp.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.simonebasile.sampleapp.model.SessionData;

/**
 * Repository for managing user sessions.
 */
public class SessionRepository {
    private final MongoCollection<SessionData> sessionCollection;

    public SessionRepository(MongoCollection<SessionData> sessionCollection) {
        this.sessionCollection = sessionCollection;
    }

    /**
     * Gets a session.
     * @param sessionId the session id
     * @return the session
     */
    public SessionData getSession(String sessionId) {
        return sessionCollection.find(Filters.eq("_id", sessionId)).first();
    }

    /**
     * Creates a session.
     * @param sessionData the session data
     */
    public void createSession(SessionData sessionData) {
        sessionCollection.insertOne(sessionData);
    }

    /**
     * Updates a session.
     * @param sessionData the session data
     * @return the updated session
     */
    public SessionData update(SessionData sessionData) {
        sessionCollection.updateOne(Filters.eq("_id", sessionData.getId()),
                Updates.set("username", sessionData.getUsername()));
        return sessionData;
    }
}
