package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class LoginControllerTest {

    private LoginController controller;
    private AuthenticationService mockAuthService;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthenticationService.class);
        controller = new LoginController(mockAuthService);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/login", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext ctx = new ApplicationRequestContext();
        HttpResponse<?> response = controller.handleGet(request, ctx);
        assertNotNull(response);
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleGet_logged() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/login", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext ctx = new ApplicationRequestContext();
        ctx.setLoggedUser(new User("user123", "password", null));
        HttpResponse<?> response = controller.handleGet(request, ctx);
        assertNotNull(response);
        assertEquals(303, response.getStatusCode());
    }

    @Test
    void testHandlePost() {
        String body = "username=user123&password=password";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/login", HttpVersion.V1_1, new HttpHeaders(), inputStream);
        ApplicationRequestContext applicationRequestContext = new ApplicationRequestContext();
        applicationRequestContext.setSessionId("SessionId");


        HttpResponse<?> response = controller.handlePost(request, applicationRequestContext);
        assertNotNull(response);
        verify(mockAuthService, times(1)).login(eq("SessionId"), any(LoginRequest.class));
    }

    @Test
    void testHandlePost_failed() {
        String body = "username=user123&password=password";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/login", HttpVersion.V1_1, new HttpHeaders(), inputStream);
        ApplicationRequestContext applicationRequestContext = new ApplicationRequestContext();
        applicationRequestContext.setSessionId("SessionId");

        doThrow(new UserAuthException("Error")).when(mockAuthService).login(eq("SessionId"), any(LoginRequest.class));

        HttpResponse<?> response = controller.handlePost(request, applicationRequestContext);
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        verify(mockAuthService, times(1)).login(eq("SessionId"), any(LoginRequest.class));
    }
}