package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.sampleapp.dto.MessageResponse;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.service.AuthenticationService;

public class RegisterHandler extends JsonBodyHandler<RegisterRequest> {

    private final AuthenticationService authService;

    public RegisterHandler(AuthenticationService authService) {
        super(RegisterRequest.class);
        this.authService = authService;
    }

    @Override
    public MappableHttpResponse<MessageResponse> handleRequest(HttpRequest<RegisterRequest> req) {
        RegisterRequest body = req.getBody();
        if(authService.register(body)) {
            return new MappableHttpResponse<>(req.getVersion(), 200,
                    new HttpHeaders(), new MessageResponse("Created"));
        } else {
            return new MappableHttpResponse<>(req.getVersion(), 400,
                    new HttpHeaders(), new MessageResponse("User already exists"));
        }
    }

}
