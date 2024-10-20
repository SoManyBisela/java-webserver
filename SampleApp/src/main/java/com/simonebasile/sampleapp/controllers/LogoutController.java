package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.service.SessionService;

import java.io.InputStream;

public class LogoutController extends MethodHandler<InputStream> {
    private final SessionService sessionService;

    public LogoutController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        sessionService.currentSession().setUsername(null);
        sessionService.updateSession();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/login");
        return new HttpResponse<>(r.getVersion(), 303, headers,null);
    }
}
