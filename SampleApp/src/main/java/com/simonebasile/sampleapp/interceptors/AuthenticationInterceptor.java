package com.simonebasile.sampleapp.interceptors;

import com.simonebasile.http.handlers.HttpInterceptor;
import com.simonebasile.http.handlers.HttpRequestHandler;
import com.simonebasile.http.response.ByteResponseBody;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.http.message.HttpHeaders;
import com.simonebasile.http.message.HttpRequest;
import com.simonebasile.http.message.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.UserService;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor to handle authentication.
 */
@Slf4j
public class AuthenticationInterceptor<T> implements HttpInterceptor<T, ApplicationRequestContext> {
    private final SessionService sessionService;
    private final UserService userService;

    public AuthenticationInterceptor(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    /**
     * Intercepts the request, checks if the user is logged in and initialized the session for the request.
     * If the user is not logged in, the request is redirected to the login page.
     * Sets the session cookie in the response.
     * @param request the request
     * @param context the context
     * @param next the next handler
     * @return the response
     */
    @Override
    public HttpResponse<? extends HttpResponseBody> intercept(HttpRequest<? extends T> request, ApplicationRequestContext context, HttpRequestHandler<T, ApplicationRequestContext> next) {
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
