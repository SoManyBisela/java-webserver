package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.LoginException;
import com.simonebasile.sampleapp.views.RegisterView;

import java.io.InputStream;

public class RegisterController extends MethodHandler<InputStream> {
    AuthenticationService authService;

    public RegisterController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        return ResponseUtils.fromView(r.getVersion(), new RegisterView());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        RegisterRequest body = FormHttpMapper.map(r.getBody(), RegisterRequest.class);
        try {
            authService.register(body);
            return ResponseUtils.redirect(r.getVersion(), "/login");
        } catch (LoginException e) {
            return ResponseUtils.fromView(r.getVersion(), 400, new RegisterView(e.getMessage()));
        }
    }
}
