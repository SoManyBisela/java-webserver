package com.simonebasile.sampleapp.controllers.htmx;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.views.htmx.UserView;

import java.io.InputStream;

public class HomeController extends MethodHandler<InputStream> {

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        return new HttpResponse<>(r.getVersion(), new UserView());
    }
}
