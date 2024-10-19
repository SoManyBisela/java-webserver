package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.HttpInterceptor;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpRequestHandler;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.service.SessionService;

public class SessionInterceptor<T> implements HttpInterceptor<T> {
    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<T> request, HttpRequestHandler<T> next) {
        String sessionCookie = request.getHeaders().getCookie("session");
        sessionCookie = sessionService.loadSession(sessionCookie);
        HttpResponse<? extends HttpResponse.ResponseBody> response = next.handle(request);
        response.getHeaders().setCookie("session", sessionCookie);
        sessionService.unloadSession();
        return response;
    }
}
