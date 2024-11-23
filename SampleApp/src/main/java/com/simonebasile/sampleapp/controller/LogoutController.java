package com.simonebasile.sampleapp.controller;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.service.SessionService;

import java.io.InputStream;

/**
 * Controller handling the logout
 */
public class LogoutController implements HttpRequestHandler<InputStream, ApplicationRequestContext> {
    private final SessionService sessionService;

    public LogoutController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * Handles the request.
     * removes the username from the session
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> handle(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        sessionService.updateSession(context.getSessionId(), s -> s.setUsername(null));
        return ResponseUtils.redirect(r, "/login");
    }
}
