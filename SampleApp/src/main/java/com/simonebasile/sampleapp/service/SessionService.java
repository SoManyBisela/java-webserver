package com.simonebasile.sampleapp.service;

import com.simonebasile.repository.SessionRepository;
import com.simonebasile.sampleapp.dto.SessionData;

import java.util.UUID;

public class SessionService {

    private final ThreadLocal<SessionData> sessionData;
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionData = new ThreadLocal<>();
    }

    public String loadSession(String sessionCookie) {
        SessionData s = getOrCreateSession(sessionCookie);
        sessionData.set(s);
        return s.getId();
    }

    public SessionData currentSession() {
        return sessionData.get();
    }

    private SessionData getOrCreateSession(String sessionId) {
        SessionData data = sessionRepository.getSession(sessionId);
        if(data == null) {
            String string = UUID.randomUUID().toString();
            SessionData sessionData = new SessionData(string);
            sessionRepository.createSession(sessionData);
        }
        return data;
    }

    public void unloadSession() {
        sessionData.remove();
    }
}
