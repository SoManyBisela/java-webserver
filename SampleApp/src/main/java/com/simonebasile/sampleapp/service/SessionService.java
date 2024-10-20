package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.DebugRegistry;
import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.model.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.function.Consumer;

public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final ThreadLocal<SessionData> sessionData;
    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.sessionData = new ThreadLocal<>();
        DebugRegistry.add("sessionService", this);
    }

    public String loadSession(String sessionCookie) {
        SessionData s = getOrCreateSession(sessionCookie);
        sessionData.set(s);
        return s.getId();
    }

    public SessionData currentSession() {
        return sessionData.get();
    }

    public SessionData updateSession(Consumer<SessionData> op) {
        SessionData data = sessionData.get();
        op.accept(data);
        return sessionRepository.update(data);
    }

    private SessionData getOrCreateSession(String sessionId) {
        log.debug("Requested session: {}", sessionId);
        SessionData data = sessionRepository.getSession(sessionId);
        if(data == null) {
            String string = UUID.randomUUID().toString();
            data = new SessionData(string);
            sessionRepository.createSession(data);
            log.info("Created new session: {}", data.getId());
        }
        log.trace("Returned session: {}", data);
        return data;
    }

    public void unloadSession() {
        sessionData.remove();
    }
}
