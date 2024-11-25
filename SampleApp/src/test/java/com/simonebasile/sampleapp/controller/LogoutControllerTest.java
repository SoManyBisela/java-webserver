package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class LogoutControllerTest {

    private LogoutController controller;
    private SessionService mockSessionService;

    @BeforeEach
    void setUp() {
        mockSessionService = mock(SessionService.class);
        controller = new LogoutController(mockSessionService);
    }

    @Test
    void testHandle() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/logout", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext applicationRequestContext = new ApplicationRequestContext();
        applicationRequestContext.setSessionId("SessionId1");
        HttpResponse<?> response = controller.handle(request, applicationRequestContext);
        assertNotNull(response);
        verify(mockSessionService, times(1)).updateSession(eq("SessionId1"), any());
    }
}