package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
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
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/site/index.html");
            return new HttpResponse<>(request.getVersion(), 302, headers, new ByteResponseBody("Unauthorized"));
        }
        return next.handle(request);
    }
}
