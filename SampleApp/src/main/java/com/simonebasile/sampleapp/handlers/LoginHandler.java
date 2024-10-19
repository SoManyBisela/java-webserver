package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.service.AuthenticationService;

public class LoginHandler extends FormHttpRequestHandler<LoginRequest> {

    private final AuthenticationService authService;

    public LoginHandler(AuthenticationService userService) {
        super(LoginRequest.class);
        this.authService = userService;
    }

    @Override
    protected MappableHttpResponse<HttpResponse.ResponseBody> handleRequest(HttpRequest<LoginRequest> req) {
        LoginRequest body = req.getBody();
        HttpHeaders headers = new HttpHeaders();
        if(authService.login(body)) {
            headers.add("Location", "/logged.html");
        } else {
            headers.add("Location", "/index.html");
        }
        return new MappableHttpResponse<>(req.getVersion(), 302, headers,null);
    }

}
