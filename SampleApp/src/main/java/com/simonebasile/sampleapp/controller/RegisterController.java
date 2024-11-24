package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.ResponseBody;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.RegisterView;

import java.io.InputStream;

/**
 * Controller for the registration page
 */
public class RegisterController extends MethodHandler<InputStream, ApplicationRequestContext> {
    AuthenticationService authService;

    public RegisterController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Handles the GET request.
     * Renders the registration page.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(new RegisterView());
    }

    /**
     * Handles the POST request.
     * Registers the user and redirects to the login page.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        RegisterRequest body = FormHttpMapper.map(r.getBody(), RegisterRequest.class);
        try {
            authService.registerUser(body);
            return ResponseUtils.redirect(r, "/login");
        } catch (UserAuthException e) {
            return new HttpResponse<>(400, new RegisterView(e.getMessage()));
        }
    }
}
