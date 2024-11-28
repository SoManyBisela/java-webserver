package com.simonebasile.sampleapp.controller.admin;

import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.CreateUserRequest;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AdminToolsSection;
import com.simonebasile.sampleapp.views.custom.Toast;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Controller for the admin tools section
 */
@Slf4j
public class AdminToolsController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final AuthenticationService authService;

    public AdminToolsController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    /**
     * Handlers the GET request.
     * Renders the admin tools section.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(new AdminToolsSection());
    }

    /**
     * Handles the POST request.
     * Creates a new user.
     * @param r the request
     * @param context the context
     * @return the response
     */
    protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        CreateUserRequest u = FormHttpMapper.map(r.getBody(), CreateUserRequest.class);
        try {
            authService.register(u);
            return new HttpResponse<>(new ElementGroup(
                    new AdminToolsSection(),
                    new Toast("User created successfully", "success")
            ));
        } catch (UserAuthException e ) {
            throw new ShowableException(e);
        }
    }
}
