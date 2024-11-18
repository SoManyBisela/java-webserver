package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.views.MainView;

import java.io.InputStream;

public class HomeController extends MethodHandler<InputStream, ApplicationRequestContext> {
    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(r.getVersion(), new MainView(context.getLoggedUser()));
    }
}
