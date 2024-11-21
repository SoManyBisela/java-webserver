package com.simonebasile.sampleapp.controller.admin;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.exceptions.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.AuthenticationService;
import com.simonebasile.sampleapp.service.errors.UserAuthException;
import com.simonebasile.sampleapp.views.AdminToolsSection;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.Toast;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class AdminToolsController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final AuthenticationService authService;

    public AdminToolsController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(new AdminToolsSection());
    }

    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User u = FormHttpMapper.map(r.getBody(), User.class);
        try {
            authService.register(u);
            return new HttpResponse<>(new ElementGroup(
                    new AdminToolsSection(),
                    Utils.oobAdd("main", new Toast("User created successfully", "success"))
            ));
        } catch (UserAuthException e ) {
            throw new ShowableException(e);
        }
    }
}
