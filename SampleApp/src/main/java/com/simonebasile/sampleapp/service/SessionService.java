package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.repository.SessionRepository;
import com.simonebasile.sampleapp.model.SessionData;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Service for managing user sessions.
 * Session is stored on the database.
 */
@Slf4j
public class SessionService {

    private final SessionRepository sessionRepository;

    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * Updates a session.
     * @param sessionId the session id
     * @param op the operation to perform on the session
     * @return the updated session
     */
    public SessionData updateSession(String sessionId, Consumer<SessionData> op) {
        SessionData data= sessionRepository.getSession(sessionId);
        op.accept(data);
        return sessionRepository.update(data);
    }

    /**
     * Gets a session.
     * If the session does not exist, it is created.
     * @param sessionId the session id
     * @return the session
     */
    public SessionData getOrCreateSession(String sessionId) {
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

}
