package com.simonebasile.sampleapp.repository;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import com.simonebasile.sampleapp.model.User;
import org.bson.BsonObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private MongoCollection<User> mockUserCollection;

    @InjectMocks
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUser() {
        String username = "testUser";
        User expectedUser = new User();
        expectedUser.setUsername(username);

        FindIterable mock = mock(FindIterable.class);
        when(mock.first()).thenReturn(expectedUser);
        when(mockUserCollection.find(Filters.eq("username", username))).thenReturn(mock);

        User actualUser = userRepository.getUser(username);

        assertNotNull(actualUser);
        assertEquals(expectedUser, actualUser);
    }

    @Test
    void testInsert() {
        User user = new User();
        user.setUsername("testUser");

        InsertOneResult mockResult = mock(InsertOneResult.class);
        BsonObjectId mockInsertedId = new BsonObjectId(new org.bson.types.ObjectId("507f191e810c19729de860ea"));
        when(mockResult.getInsertedId()).thenReturn(mockInsertedId);
        when(mockUserCollection.insertOne(user)).thenReturn(mockResult);

        userRepository.insert(user);

        verify(mockUserCollection, times(1)).insertOne(user);

        assertEquals("507f191e810c19729de860ea", user.getId().toHexString());
    }

    @Test
    void testInsertWithNullId() {
        User user = new User();
        user.setUsername("testUser");

        InsertOneResult mockResult = mock(InsertOneResult.class);
        when(mockResult.getInsertedId()).thenReturn(null);  // Simula un ID nullo
        when(mockUserCollection.insertOne(user)).thenReturn(mockResult);

        userRepository.insert(user);

        verify(mockUserCollection, times(1)).insertOne(user);

        assertNull(user.getId());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setUsername("testUser");

        userRepository.updateUser(user);

        verify(mockUserCollection, times(1)).replaceOne(Filters.eq("username", user.getUsername()), user);
    }
}
