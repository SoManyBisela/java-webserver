package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.message.HttpVersion;
import com.simonebasile.sampleapp.controller.admin.AdminToolsController;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.CreateUserRequest;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AdminToolsSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminToolsControllerTest {

    private AdminToolsController controller;
    private AuthenticationService mockAuthService;
    private ApplicationRequestContext mockContext;
    private User adminUser;

    @BeforeEach
    void setUp() {
        mockAuthService = mock(AuthenticationService.class);
        controller = new AdminToolsController(mockAuthService);
        mockContext = mock(ApplicationRequestContext.class);
        adminUser = new User("admin", "password", Role.admin);
        when(mockContext.getLoggedUser()).thenReturn(adminUser);
    }

    @Test
    void testHandleGet() {
        HttpRequest<InputStream> request = new HttpRequest<>("GET", "/admin/tools", HttpVersion.V1_1, new HttpHeaders(), null);
        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertInstanceOf(AdminToolsSection.class, response.getBody());
    }

    @Test
    void testHandlePost() {
        String body = "username=newuser&password=newpassword&cpassword=newpassword&role=user";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/admin/tools", HttpVersion.V1_1, new HttpHeaders(), inputStream);

        HttpResponse<?> response = controller.handle(request, mockContext);
        assertNotNull(response);
        assertNotNull(response.getBody());
        verify(mockAuthService, times(1)).register(any(CreateUserRequest.class));
    }

    @Test
    void testHandlePost_UserAuthException() {
        String body = "username=newuser&password=newpassword&cpassword=notthesameaspassword&role=user";
        InputStream inputStream = new ByteArrayInputStream(body.getBytes());
        HttpRequest<InputStream> request = new HttpRequest<>("POST", "/admin/tools", HttpVersion.V1_1, new HttpHeaders(), inputStream);

        doThrow(new UserAuthException("User authentication failed")).when(mockAuthService).register(any(CreateUserRequest.class));

        assertThrows(ShowableException.class, () -> controller.handle(request, mockContext));
    }
}