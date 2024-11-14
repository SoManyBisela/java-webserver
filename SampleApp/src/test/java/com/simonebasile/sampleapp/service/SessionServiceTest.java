package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    @Mock
    private SessionRepository mockSessionRepository;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadSession_ExistingSession() {
        String sessionId = "existingSession";
        SessionData expectedSessionData = new SessionData(sessionId);

        when(mockSessionRepository.getSession(sessionId)).thenReturn(expectedSessionData);

        String loadedSessionId = sessionService.loadSession(sessionId);

        assertEquals(sessionId, loadedSessionId);
        assertEquals(expectedSessionData, sessionService.currentSession());
        verify(mockSessionRepository).getSession(sessionId);
    }

    @Test
    void testLoadSession_NewSession() {
        String sessionId = "nonExistentSession";
        
        when(mockSessionRepository.getSession(sessionId)).thenReturn(null);
        doAnswer(invocation -> {
            SessionData sessionData = invocation.getArgument(0);
            sessionData.setId(UUID.randomUUID().toString());
            return null;
        }).when(mockSessionRepository).createSession(any(SessionData.class));

        String loadedSessionId = sessionService.loadSession(sessionId);

        assertNotNull(loadedSessionId);
        assertEquals(loadedSessionId, sessionService.currentSession().getId());
        verify(mockSessionRepository).getSession(sessionId);
        verify(mockSessionRepository).createSession(any(SessionData.class));
    }

    @Test
    void testCurrentSession_NoSessionLoaded() {
        assertNull(sessionService.currentSession());
    }

    @Test
    void testUpdateSession() {
        String sessionId = "sessionToUpdate";
        SessionData sessionData = new SessionData(sessionId);
        when(mockSessionRepository.getSession(sessionId)).thenReturn(sessionData);
        sessionService.loadSession(sessionId);
        sessionService.updateSession(s -> s.setUsername("newUsername"));
        
        verify(mockSessionRepository).update(sessionData);
        assertEquals("newUsername", sessionService.currentSession().getUsername());
    }

    @Test
    void testUnloadSession() {
        String sessionId = "sessionToUnload";
        SessionData sessionData = new SessionData(sessionId);

        when(mockSessionRepository.getSession(sessionId)).thenReturn(sessionData);
        sessionService.loadSession(sessionId);
        
        sessionService.unloadSession();

        assertNull(sessionService.currentSession());
    }
}
