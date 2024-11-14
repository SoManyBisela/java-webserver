package com.simonebasile.sampleapp.repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.simonebasile.sampleapp.model.SessionData;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionRepositoryTest {

    @Mock
    private MongoCollection<SessionData> mockSessionCollection;

    @InjectMocks
    private SessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSession() {
        String sessionId = "session123";
        SessionData expectedSession = new SessionData();
        expectedSession.setId(sessionId);

        FindIterable mock = mock(FindIterable.class);
        when(mock.first()).thenReturn(expectedSession);
        when(mockSessionCollection.find(Filters.eq("_id", sessionId))).thenReturn(mock);

        SessionData actualSession = sessionRepository.getSession(sessionId);

        assertNotNull(actualSession);
        assertEquals(expectedSession, actualSession);
    }

    @Test
    void testCreateSession() {
        SessionData sessionData = new SessionData();
        sessionData.setId("session123");

        // Act
        sessionRepository.createSession(sessionData);

        // Assert that insertOne was called with the correct sessionData
        verify(mockSessionCollection, times(1)).insertOne(sessionData);
    }

    @Test
    void testUpdateSession() {
        SessionData sessionData = new SessionData();
        sessionData.setId("session123");
        sessionData.setUsername("newUser");

        // Act
        SessionData updatedSession = sessionRepository.update(sessionData);

        // Assert that updateOne was called with the correct filters and update operations
        ArgumentCaptor<Bson> filterCaptor = ArgumentCaptor.forClass(Bson.class);
        ArgumentCaptor<Bson> updateCaptor = ArgumentCaptor.forClass(Bson.class);
        
        verify(mockSessionCollection, times(1)).updateOne(filterCaptor.capture(), updateCaptor.capture());

        // Check that the correct filter and update operations were applied
        assertEquals(Filters.eq("_id", "session123"), filterCaptor.getValue());
        assertEquals(Updates.set("username", "newUser"), updateCaptor.getValue());
        
        // Check if the returned session data matches
        assertEquals(sessionData, updatedSession);
    }
}
