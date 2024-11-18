package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AccountSection;

import java.io.InputStream;

public class AccountController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final AuthenticationService authService;

    public AccountController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        return new HttpResponse<>(new AccountSection(user));
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        ChangePasswordRequest changePasswordReq = FormHttpMapper.map(r.getBody(), ChangePasswordRequest.class);
        changePasswordReq.setUsername(user.getUsername());
        try {
            authService.changePassword(changePasswordReq);
            return new HttpResponse<>(new AccountSection(user).successMessage("Password changed successfully"));
        } catch (UserAuthException e ) {
            return new HttpResponse<>(new AccountSection(user).changePasswordError(e.getMessage()));
        }
    }
}
