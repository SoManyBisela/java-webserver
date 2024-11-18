package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.*;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationInterceptor<T> implements HttpInterceptor<T, ApplicationRequestContext> {
    private final SessionService sessionService;
    private final UserService userService;

    public AuthenticationInterceptor(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public HttpResponse<? extends HttpResponse.ResponseBody> preprocess(HttpRequest<? extends T> request, ApplicationRequestContext context, HttpRequestHandler<T, ApplicationRequestContext> next) {
        String sessionCookie = request.getHeaders().getCookie("session");
        SessionData sessionData = sessionService.getOrCreateSession(sessionCookie);
        if(sessionData == null) {
            log.error("Failed to create session data");
            return new HttpResponse<>(500, new HttpHeaders(), new ByteResponseBody("Internal server Error"));
        }
        context.setSessionId(sessionData.getId());
        if(sessionData.getUsername() != null) {
            User logged = userService.getUser(sessionData.getUsername());
            if(logged == null) {
                log.error("Logged user in session does not exist");
                return new HttpResponse<>(500, new HttpHeaders(), new ByteResponseBody("Internal server Error"));
            }
            context.setLoggedUser(logged);
        } else if (!request.getResource().equals("/login")){
            return ResponseUtils.redirect(request, "/login");
        }
        var response = next.handle(request, context);
        response.getHeaders().setCookie("session", sessionData.getId());
        return response;
    }
}
