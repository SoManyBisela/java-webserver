package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.assertions.Assertions;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.HomeView;

import java.io.InputStream;
import java.util.Optional;

public class HomeController extends MethodHandler<InputStream> {
    private final SessionService sessionService;
    private final UserService userService;

    public HomeController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        return new HttpResponse<>(r.getVersion(), new HomeView(user));
    }

}
