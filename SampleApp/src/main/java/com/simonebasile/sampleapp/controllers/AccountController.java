package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AccountSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class AccountController extends MethodHandler<InputStream> {

    private final AuthenticationService authService;
    private final SessionService sessionService;
    private final UserService userService;

    public AccountController(SessionService sessionService, UserService userService, AuthenticationService authenticationService) {
        this.authService = authenticationService;
        this.sessionService = sessionService;
        this.userService = userService;
    }


    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        return new HttpResponse<>(r.getVersion(), new AccountSection(user));
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<InputStream> r) {
        SessionData sessionData = sessionService.currentSession();
        User user = userService.getUser(sessionData.getUsername());
        ChangePasswordRequest changePasswordReq = FormHttpMapper.map(r.getBody(), ChangePasswordRequest.class);
        changePasswordReq.setUsername(user.getUsername());
        try {
            authService.changePassword(changePasswordReq);
            return new HttpResponse<>(r.getVersion(), new AccountSection(user).successMessage("Password changed successfully"));
        } catch (UserAuthException e ) {
            return new HttpResponse<>(r.getVersion(), new AccountSection(user).changePasswordError(e.getMessage()));
        }
    }
}
