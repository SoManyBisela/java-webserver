package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.LoginView;

import java.io.InputStream;

/**
 * Controller for the login page
 */
public class LoginController extends MethodHandler<InputStream, ApplicationRequestContext> {
    AuthenticationService authService;

    public LoginController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Handles the GET request.
     * Renders the login page.
     * If the user is already logged in, redirects to the home page.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        if(context.getLoggedUser() != null) {
            return Utils.redirect(r, "/");
        }
        return new HttpResponse<>(new LoginView());
    }

    /**
     * Handles the POST request.
     * Logs in the user updating the session and redirecting to the home page.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        LoginRequest body = FormHttpMapper.map(r.getBody(), LoginRequest.class);
        try {
            authService.login(context.getSessionId(), body);
            return Utils.redirect(r, "/");
        } catch (UserAuthException e) {
            return new HttpResponse<>(401, new LoginView(e.getMessage()));
        }
    }
}
