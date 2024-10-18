package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.MessageResponse;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;

public class LoginHandler extends JsonBodyHandler<LoginRequest> {

    private final UserRepository userRepository;

    public LoginHandler(UserRepository userRepository) {
        super(LoginRequest.class);
        this.userRepository = userRepository;
    }

    @Override
    public MappableHttpResponse<MessageResponse> handleRequest(HttpRequest<LoginRequest> req) {
        LoginRequest body = req.getBody();
        return userRepository.getUser(body.getUsername()).map(u -> {
            if(ArgonUtils.verify(body.getPassword(), u.getPassword())) {
                return new MappableHttpResponse<>(req.getVersion(), 200, createSessionHeaders(u), new MessageResponse("Ok"));
            } else {
                return null;
            }
        }).orElseGet(() -> new MappableHttpResponse<>(
                req.getVersion(), 401, new HttpHeaders(), new MessageResponse("Unauthorized"))
        );
    }

    private HttpHeaders createSessionHeaders(User u) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setCookie("user", u.getUsername());
        return httpHeaders;
    }
}
