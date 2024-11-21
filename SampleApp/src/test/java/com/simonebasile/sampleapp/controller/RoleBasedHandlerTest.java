package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.*;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.exceptions.ShowableException;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RoleBasedHandlerTest {

    HttpRequestHandler<InputStream, ApplicationRequestContext> userHandler;
    HttpRequestHandler<InputStream, ApplicationRequestContext> adminHandler;
    RoleBasedHandler handler;


    @BeforeEach
    void setUp() {
        userHandler = mock(HttpRequestHandler.class);
        adminHandler = mock(HttpRequestHandler.class);
        handler = RoleBasedHandler.builder()
                .handle(Role.user, userHandler)
                .handle(Role.admin, adminHandler)
                .build();
    }

    @Test
    void testHandleUserRole() {

        HttpRequest<InputStream> req = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        User user = new User("user123", "password", Role.user);
        ApplicationRequestContext ctx = new ApplicationRequestContext();
        ctx.setLoggedUser(user);

        handler.handle(req, ctx);

        verify(userHandler, times(1)).handle(req, ctx);
        verify(adminHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
    }

    @Test
    void testHandleAdminRole() {

        HttpRequest<InputStream> req = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        User user = new User("admin", "password", Role.admin);
        ApplicationRequestContext ctx = new ApplicationRequestContext();
        ctx.setLoggedUser(user);

        handler.handle(req, ctx);

        verify(adminHandler, times(1)).handle(req, ctx);
        verify(userHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
    }

    @Test
    void testHandleUnhandlerdRole() {

        HttpRequest<InputStream> req = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        User user = new User("employee", "password", Role.employee);
        ApplicationRequestContext ctx = new ApplicationRequestContext();
        ctx.setLoggedUser(user);

        assertThrows(ShowableException.class, () -> handler.handle(req, ctx));

        verify(adminHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
        verify(userHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
    }

    @Test
    void testHandleNotlogged() {

        HttpRequest<InputStream> req = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext ctx = new ApplicationRequestContext();

        assertThrows(ShowableException.class, () -> handler.handle(req, ctx));

        verify(adminHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
        verify(userHandler, times(0)).handle(any(HttpRequest.class), any(ApplicationRequestContext.class));
    }

    @Test
    void testContextCreation() {
        RoleBasedHandler roleBasedHandler = RoleBasedHandler.of(Role.admin, adminHandler);
        HttpRequest<InputStream> req = new HttpRequest<>("GET", "/", HttpVersion.V1_1, new HttpHeaders(), null);
        ApplicationRequestContext ctx = new ApplicationRequestContext();

        User admin = new User("admin", "password", Role.admin);
        ctx.setLoggedUser(admin);
        roleBasedHandler.handle(req, ctx);
        verify(adminHandler, times(1)).handle(req, ctx);

        User user = new User("user123", "password", Role.user);
        ctx.setLoggedUser(user);
        Assertions.assertThrows(ShowableException.class, () -> roleBasedHandler.handle(req, ctx));

        Assertions.assertThrows(IllegalArgumentException.class, () -> roleBasedHandler.builder()
                .handle(Role.admin, adminHandler)
                .handle(Role.admin, adminHandler)
                .build());
    }
}