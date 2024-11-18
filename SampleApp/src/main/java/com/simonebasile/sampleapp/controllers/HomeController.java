package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.MainView;

import java.io.InputStream;

public class HomeController extends MethodHandler<InputStream> {
    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(r.getVersion(), new MainView(context.getLoggedUser()));
    }
}
