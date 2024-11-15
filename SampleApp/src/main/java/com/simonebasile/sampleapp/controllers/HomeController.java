package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.MainView;

import java.io.InputStream;

public class HomeController extends MethodHandler<InputStream> {
    private final SessionService sessionService;
    private final UserService userService;

    public HomeController (SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r) {
        final String username = sessionService.currentSession().getUsername();
        final User user = userService.getUser(username);
        return new HttpResponse<>(r.getVersion(), new MainView(user));
    }
}
