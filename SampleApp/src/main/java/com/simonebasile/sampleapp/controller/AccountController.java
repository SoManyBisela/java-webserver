package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AccountSection;
import com.simonebasile.sampleapp.views.custom.Toast;
import com.simonebasile.web.ssr.component.ElementGroup;

import java.io.InputStream;

/**
 * Controller for the account section
 */
public class AccountController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final AuthenticationService authService;

    public AccountController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    /**
     * Handles the GET request.
     * Renders the account section.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        return new HttpResponse<>(new AccountSection(user));
    }

    /**
     * Handles the POST request.
     * Changes the password.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        ChangePasswordRequest changePasswordReq = FormHttpMapper.map(r.getBody(), ChangePasswordRequest.class);
        changePasswordReq.setUsername(user.getUsername());
        try {
            authService.changePassword(changePasswordReq);
            return new HttpResponse<>(new ElementGroup(
                    new AccountSection(user),
                    new Toast("Password changed successfully", "success")
            ));
        } catch (UserAuthException e ) {
            throw new ShowableException(e);
        }
    }
}
