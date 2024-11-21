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
    void testUpdateSession() {
        String sessionId = "session123";
        SessionData sessionData = new SessionData(sessionId);

        when(mockSessionRepository.getSession(sessionId)).thenReturn(sessionData);
        when(mockSessionRepository.update(sessionData)).thenReturn(sessionData);

        sessionService.updateSession(sessionId, data -> data.setUsername("newUsername"));

        verify(mockSessionRepository).getSession(sessionId);
        verify(mockSessionRepository).update(sessionData);
        assertEquals("newUsername", sessionData.getUsername());
    }

    @Test
    void testGetOrCreateSession_SessionExists() {
        String sessionId = "session123";
        SessionData sessionData = new SessionData(sessionId);

        when(mockSessionRepository.getSession(sessionId)).thenReturn(sessionData);

        SessionData result = sessionService.getOrCreateSession(sessionId);

        verify(mockSessionRepository).getSession(sessionId);
        assertEquals(sessionData, result);
    }

    @Test
    void testGetOrCreateSession_SessionDoesNotExist() {
        String sessionId = "session123";

        when(mockSessionRepository.getSession(sessionId)).thenReturn(null);

        SessionData result = sessionService.getOrCreateSession(sessionId);

        verify(mockSessionRepository).getSession(sessionId);
        verify(mockSessionRepository).createSession(any(SessionData.class));
        assertNotNull(result);
        assertNotNull(result.getId());
    }
}