package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class RegisterControllerTest {

    private RegisterController controller;
    private AuthenticationService mockAuthService;
    private ApplicationRequestContext mockContext;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthenticationService.class);
        controller = new RegisterController(mockAuthService);
        mockContext = mock(ApplicationRequestContext.class);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/register", HttpVersion.V1_1, new HttpHeaders(), null);
        HttpResponse<?> response = controller.handleGet(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
    }

    @Test
    void testHandlePost() {
        String body = "username=user123&password=password&email=user@example.com";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/register", HttpVersion.V1_1, new HttpHeaders(), inputStream);

        HttpResponse<?> response = controller.handlePost(request, mockContext);
        assertNotNull(response);
        verify(mockAuthService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testHandlePost_Failed() {
        String body = "username=user123&password=password&email=user@example.com";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/register", HttpVersion.V1_1, new HttpHeaders(), inputStream);

        doThrow(new UserAuthException("Error")).when(mockAuthService).registerUser(any(RegisterRequest.class));

        HttpResponse<?> response = controller.handlePost(request, mockContext);
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        verify(mockAuthService, times(1)).registerUser(any(RegisterRequest.class));
    }

}