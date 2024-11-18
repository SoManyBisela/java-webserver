package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.LoginView;

import java.io.InputStream;

public class LoginController extends MethodHandler<InputStream, ApplicationRequestContext> {
    AuthenticationService authService;

    public LoginController(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        if(context.getLoggedUser() != null) {
            return ResponseUtils.redirect(r, "/");
        }
        return new HttpResponse<>(r.getVersion(), new LoginView());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        LoginRequest body = FormHttpMapper.map(r.getBody(), LoginRequest.class);
        try {
            authService.login(context.getSessionId(), body);
            return ResponseUtils.redirect(r, "/");
        } catch (UserAuthException e) {
            return new HttpResponse<>(r.getVersion(), 401, new LoginView(e.getMessage()));
        }
    }
}
