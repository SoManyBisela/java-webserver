package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.HttpVersion;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class HomeControllerTest {

    private HomeController controller;
    private ApplicationRequestContext mockContext;
    private User user;

    @BeforeEach
    void setUp() {
        controller = new HomeController();
        mockContext = mock(ApplicationRequestContext.class);
        user = new User("user123", "password", Role.user);
        when(mockContext.getLoggedUser()).thenReturn(user);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        HttpResponse<?> response = controller.handleGet(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockContext, times(1)).getLoggedUser();
    }
}