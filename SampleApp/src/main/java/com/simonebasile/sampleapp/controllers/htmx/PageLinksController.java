package com.simonebasile.sampleapp.controllers.htmx;


import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.htmx.HtmxResponse;
import com.simonebasile.sampleapp.views.htmx.NavButton;
import com.simonebasile.sampleapp.views.htmx.SidebarButtons;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PageLinksController extends MethodHandler<InputStream> {
    private final SessionService sessionService;
    private final UserService userService;

    public PageLinksController(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<InputStream> r) {
        final String username = sessionService.currentSession().getUsername();
        final User user = userService.getUser(username);
        return new HttpResponse<>(r.getVersion(), new SidebarButtons(user));
    }
}
