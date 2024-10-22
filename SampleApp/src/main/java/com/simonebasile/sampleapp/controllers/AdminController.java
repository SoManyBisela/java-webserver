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
import com.simonebasile.sampleapp.views.HomeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class AdminController extends MethodHandler<InputStream> {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final AuthenticationService authService;
    private final SessionService sessionService;
    private final UserService userService;

    public AdminController(AuthenticationService authService, SessionService sessionService, UserService userService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() != Role.admin) {
            log.warn("Unauthorized access to POST /admin/newuser from user {}", user.getUsername());
            return ResponseUtils.redirect(r.getVersion(), "/");
        }
        User u = FormHttpMapper.map(r.getBody(), User.class);
        try {
            authService.register(u);
            return ResponseUtils.fromView(r.getVersion(), new HomeView(user));
        } catch (UserAuthException e ) {
            return ResponseUtils.fromView(r.getVersion(), new HomeView(user, new HomeView.Errors(e.getMessage())));
        }
    }
}
