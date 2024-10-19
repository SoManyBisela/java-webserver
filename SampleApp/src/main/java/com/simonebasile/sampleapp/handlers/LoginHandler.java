package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.MessageResponse;
import com.simonebasile.sampleapp.service.AuthenticationService;

public class LoginHandler extends JsonBodyHandler<LoginRequest> {

    private final AuthenticationService authService;

    public LoginHandler(AuthenticationService userService) {
        super(LoginRequest.class);
        this.authService = userService;
    }

    @Override
    public MappableHttpResponse<MessageResponse> handleRequest(HttpRequest<LoginRequest> req) {
        LoginRequest body = req.getBody();
        if(authService.login(body)) {
            return new MappableHttpResponse<>(req.getVersion(), 200, new HttpHeaders(), new MessageResponse("Ok"));
        } else {
            return new MappableHttpResponse<>(req.getVersion(), 401, new HttpHeaders(), new MessageResponse("Unauthorized"));
        }
    }

}
