package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.chat.ChatSection;

import java.io.InputStream;

public class ChatController extends MethodHandler<InputStream> {
    private final SessionService sessionService;
    private final UserService userService;

    public ChatController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        if(user.getRole() == Role.employee || user.getRole() == Role.user) {
            return new HttpResponse<>(r.getVersion(), new ChatSection());
        } else {
            return new HttpResponse<>(r.getVersion(), null);
        }
    }
}
