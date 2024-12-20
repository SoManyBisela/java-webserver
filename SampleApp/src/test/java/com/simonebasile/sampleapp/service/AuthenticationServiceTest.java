package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.security.ArgonUtils;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository mockUserRepository;

    @Mock
    private SessionService mockSessionService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        String username = "testUser";
        String password = "password123";
        String hashedPassword = ArgonUtils.hash(password);

        String sessionId = "sessionId123";

        LoginRequest request = new LoginRequest(username, password);
        User mockUser = new User(username, hashedPassword, Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(mockUser);

        authenticationService.login(sessionId, request);

        verify(mockSessionService).updateSession(eq(sessionId), any());
    }

    @Test
    void testLogin_UserNotFound() {
        String username = "nonExistentUser";
        String password = "password123";
        String sessionId = "sessionId123";

        LoginRequest request = new LoginRequest(username, password);

        when(mockUserRepository.getUser(username)).thenReturn(null);

        assertThrows(UserAuthException.class, () -> authenticationService.login(sessionId, request));
    }

    @Test
    void testLogin_InvalidPassword() {
        String username = "testUser";
        String password = "wrongPassword";
        String hashedPassword = ArgonUtils.hash("correctPassword");
        String sessionId = "sessionId123";

        LoginRequest request = new LoginRequest(username, password);
        User mockUser = new User(username, hashedPassword, Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(mockUser);

        assertThrows(UserAuthException.class, () -> authenticationService.login(sessionId, request));
    }

    @Test
    void testRegisterUser_Success() {
        String username = "newUser";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, password, password);

        when(mockUserRepository.getUser(username)).thenReturn(null);

        authenticationService.registerUser(request);

        verify(mockUserRepository).insert(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        String username = "existingUser";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, password, password);
        User existingUser = new User(username, "hashedPassword", Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(existingUser);

        assertThrows(UserAuthException.class, () -> authenticationService.registerUser(request));
    }

    @Test
    void testChangePassword_Success() {
        String username = "testUser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = ArgonUtils.hash(oldPassword);

        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword, newPassword);
        User mockUser = new User(username, hashedOldPassword, Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(mockUser);

        authenticationService.changePassword(request);

        verify(mockUserRepository).updateUser(mockUser);
        assertTrue(ArgonUtils.verify(newPassword, mockUser.getPassword()));
    }

    @Test
    void testChangePassword_InvalidOldPassword() {
        String username = "testUser";
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = ArgonUtils.hash("CorrectOldPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword, newPassword);
        User mockUser = new User(username, hashedOldPassword, Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(mockUser);

        assertThrows(UserAuthException.class, () -> authenticationService.changePassword(request));
    }

    @Test
    void testChangePassword_EmptyNewPassword() {
        String username = "testUser";
        String oldPassword = "oldPassword";
        String newPassword = "";
        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword, newPassword);

        assertThrows(UserAuthException.class, () -> authenticationService.changePassword(request));
    }

    @Test
    void testChangePassword_PasswordsDoNotMatch() {
        String username = "testUser";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String confirmPassword = "differentPassword";
        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword, confirmPassword);

        assertThrows(UserAuthException.class, () -> authenticationService.changePassword(request));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        String username = "testUser";
        String oldPassword = "wrongOldPassword";
        String newPassword = "newPassword";
        String hashedOldPassword = ArgonUtils.hash("correctOldPassword");
        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword, newPassword);
        User mockUser = new User(username, hashedOldPassword, Role.user);

        when(mockUserRepository.getUser(username)).thenReturn(mockUser);

        assertThrows(UserAuthException.class, () -> authenticationService.changePassword(request));
    }

    @Test
    void testRegisterUser_InvalidUsername() {
        String username = "";
        String password = "password123";

        RegisterRequest request = new RegisterRequest(username, password, password);

        assertThrows(UserAuthException.class, () -> authenticationService.registerUser(request));
    }

    @Test
    void testRegisterUser_InvalidPassword() {
        String username = "newUser";
        String password = "";

        RegisterRequest request = new RegisterRequest(username, password, password);

        assertThrows(UserAuthException.class, () -> authenticationService.registerUser(request));
    }

    @Test
    void testRegisterUser_PasswordsDoNotMatch() {
        String username = "newUser";
        String password = "password123";
        String confirmPassword = "differentPassword";

        RegisterRequest request = new RegisterRequest(username, password, confirmPassword);

        assertThrows(UserAuthException.class, () -> authenticationService.registerUser(request));
    }
}
