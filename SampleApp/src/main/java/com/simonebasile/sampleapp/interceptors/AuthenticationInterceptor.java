package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.sampleapp.dto.SessionData;
import com.simonebasile.sampleapp.service.SessionService;

import java.io.InputStream;

public class AuthenticationInterceptor implements HttpInterceptor<InputStream> {

    private final SessionService sessionService;

    public AuthenticationInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<InputStream> request, HttpRequestHandler<InputStream> next) {
        SessionData sessionData = sessionService.currentSession();
        if(sessionData == null || sessionData.getUsername() == null) {
            return new HttpResponse<>(request.getVersion(), 401, new HttpHeaders(), new ByteResponseBody("Unauthorized"));
        }
        return next.handle(request);
    }
}
