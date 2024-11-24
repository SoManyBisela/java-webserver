package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationInterceptorTest {

    @Mock
    private SessionService mockSessionService;

    @Mock
    private UserService mockUserService;

    @Mock
    private HttpRequestHandler<String, ApplicationRequestContext> mockNextHandler;

    private AuthenticationInterceptor<String> authenticationInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationInterceptor = new AuthenticationInterceptor<>(mockSessionService, mockUserService);
    }

    @Test
    void testIntercept_SessionExists_UserExists() {
        HttpRequest<String> request = new HttpRequest<>("GET", "/api/resource", HttpVersion.V1_1,
                new HttpHeaders().add("cookie", "session=session123"),
                null);
        ApplicationRequestContext context = new ApplicationRequestContext();
        SessionData sessionData = new SessionData("session123");
        sessionData.setUsername("user123");
        User user = new User("password123", "User", Role.user);

        when(mockSessionService.getOrCreateSession("session123")).thenReturn(sessionData);
        when(mockUserService.getUser("user123")).thenReturn(user);
        when(mockNextHandler.handle(request, context)).thenReturn(new HttpResponse<>(200, new HttpHeaders(), null));

        HttpResponse<? extends ResponseBody> response = authenticationInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(200, response.getStatusCode());
        assertEquals("session123", context.getSessionId());
        assertEquals(user, context.getLoggedUser());
    }

    @Test
    void testIntercept_SessionDoesNotExist() {
        HttpRequest<String> request = new HttpRequest<>("GET",
                "/api/resource",
                HttpVersion.V1_1,
                new HttpHeaders().add("cookie", "session=session123"),
                null);
        ApplicationRequestContext context = new ApplicationRequestContext();

        when(mockSessionService.getOrCreateSession("session123")).thenReturn(null);

        HttpResponse<? extends ResponseBody> response = authenticationInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(500, response.getStatusCode());
    }

    @Test
    void testIntercept_UserDoesNotExist() {
        HttpRequest<String> request = new HttpRequest<>("GET",
                "/api/resource",
                HttpVersion.V1_1,
                new HttpHeaders().add("cookie", "session=session123"),
                null);
        ApplicationRequestContext context = new ApplicationRequestContext();
        SessionData sessionData = new SessionData("session123");
        sessionData.setUsername("user123");

        when(mockSessionService.getOrCreateSession("session123")).thenReturn(sessionData);
        when(mockUserService.getUser("user123")).thenReturn(null);

        HttpResponse<? extends ResponseBody> response = authenticationInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(500, response.getStatusCode());
    }

    @Test
    void testIntercept_RedirectToLogin() {
        HttpRequest<String> request = new HttpRequest<>("GET",
                "/api/resource",
                HttpVersion.V1_1,
                new HttpHeaders().add("cookie", "session=session123"),
                null);
        ApplicationRequestContext context = new ApplicationRequestContext();
        SessionData sessionData = new SessionData("session123");

        when(mockSessionService.getOrCreateSession("session123")).thenReturn(sessionData);

        HttpResponse<? extends ResponseBody> response = authenticationInterceptor.intercept(request, context, mockNextHandler);

        assertEquals(303, response.getStatusCode());
        assertEquals("/login", response.getHeaders().getFirst("Location"));
    }
}