package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.ResponseBody;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.views.MainView;

import java.io.InputStream;

/**
 * Controller for the home page
 */
public class HomeController extends MethodHandler<InputStream, ApplicationRequestContext> {

    /**
     * Handles the GET request.
     * Renders the main view.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        return new HttpResponse<>(new MainView(context.getLoggedUser()));
    }
}
