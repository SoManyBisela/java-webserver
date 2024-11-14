package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUser_ExistingUser() {
        String username = "user123";
        User expectedUser = new User(username, "hashedPassword", Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(expectedUser);

        User result = userService.getUser(username);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        assertEquals(username, result.getUsername());
        verify(mockUserRepository).getUser(username);
    }

    @Test
    void testGetUser_NonExistingUser() {
        String username = "nonExistentUser";

        when(mockUserRepository.getUser(username)).thenReturn(null);

        User result = userService.getUser(username);

        assertNull(result);
        verify(mockUserRepository).getUser(username);
    }
}
