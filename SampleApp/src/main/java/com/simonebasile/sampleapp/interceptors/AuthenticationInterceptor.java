package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.service.SessionService;

public class AuthenticationInterceptor<T> implements HttpInterceptor<T> {

    private final SessionService sessionService;

    public AuthenticationInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<T> request, HttpRequestHandler<T> next) {
        SessionData sessionData = sessionService.currentSession();
        if(sessionData == null || sessionData.getUsername() == null) {
            return ResponseUtils.redirect(request.getVersion(), "/login");
        }
        return next.handle(request);
    }
}
