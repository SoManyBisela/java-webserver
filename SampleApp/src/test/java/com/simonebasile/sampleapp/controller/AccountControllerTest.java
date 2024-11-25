package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    private AccountController controller;
    private AuthenticationService mockAuthService;
    private ApplicationRequestContext mockContext;
    private User user;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthenticationService.class);
        controller = new AccountController(mockAuthService);
        mockContext = mock(ApplicationRequestContext.class);
        user = new User("user123", "password", Role.user);
        when(mockContext.getLoggedUser()).thenReturn(user);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/account", HttpVersion.V1_1, new HttpHeaders(), null);
        HttpResponse<?> response = controller.handleGet(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockContext, times(1)).getLoggedUser();
    }

    @Test
    void testHandlePost() {
        String body = "oldPassword=oldpassword&newPassword=newpassword";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/account", HttpVersion.V1_1, new HttpHeaders(), inputStream);

        HttpResponse<?> response = controller.handlePost(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockAuthService, times(1)).changePassword(any(ChangePasswordRequest.class));
    }
}