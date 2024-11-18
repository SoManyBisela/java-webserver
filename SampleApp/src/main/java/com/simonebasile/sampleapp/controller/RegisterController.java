package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.RegisterView;

import java.io.InputStream;

public class RegisterController extends MethodHandler<InputStream, ApplicationRequestContext> {
    AuthenticationService authService;

    public RegisterController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(new RegisterView());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        RegisterRequest body = FormHttpMapper.map(r.getBody(), RegisterRequest.class);
        try {
            authService.registerUser(body);
            return ResponseUtils.redirect(r, "/login");
        } catch (UserAuthException e) {
            return new HttpResponse<>(400, new RegisterView(e.getMessage()));
        }
    }
}
