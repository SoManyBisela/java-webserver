package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.LoginView;

import java.io.InputStream;

public class LoginController extends MethodHandler<InputStream> {
    AuthenticationService authService;

    public LoginController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        return new HttpResponse<>(r.getVersion(), new LoginView());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        LoginRequest body = FormHttpMapper.map(r.getBody(), LoginRequest.class);
        try {
            authService.login(body);
            return ResponseUtils.redirect(r, "/");
        } catch (UserAuthException e) {
            return new HttpResponse<>(r.getVersion(), 401, new LoginView(e.getMessage()));
        }
    }
}
