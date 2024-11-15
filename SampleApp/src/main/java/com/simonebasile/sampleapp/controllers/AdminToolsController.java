package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AdminToolsSection;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

@Slf4j
public class AdminToolsController extends MethodHandler<InputStream> {


    private final AuthenticationService authService;
    private final SessionService sessionService;
    private final UserService userService;

    public AdminToolsController(SessionService sessionService, UserService userService, AuthenticationService authenticationService) {
        this.authService = authenticationService;
        this.sessionService = sessionService;
        this.userService = userService;
    }


    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.admin) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
        return new HttpResponse<>(r.getVersion(), new AdminToolsSection());
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.admin) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            return ResponseUtils.redirect(r, "/");
        }
        User u = FormHttpMapper.map(r.getBody(), User.class);
        try {
            authService.register(u);
            return new HttpResponse<>(r.getVersion(), new AdminToolsSection().successMessage("User created successfully"));
        } catch (UserAuthException e ) {
            return new HttpResponse<>(r.getVersion(), new AdminToolsSection().createUserError(e.getMessage()));
        }
    }
}
