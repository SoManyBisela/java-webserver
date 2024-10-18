package com.simonebasile.sampleapp.handlers;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.MessageResponse;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;

import java.util.Optional;

public class RegisterHandler extends JsonBodyHandler<RegisterRequest> {

    private final UserRepository userRepository;

    public RegisterHandler(UserRepository userRepository) {
        super(RegisterRequest.class);
        this.userRepository = userRepository;
    }

    @Override
    public MappableHttpResponse<MessageResponse> handleRequest(HttpRequest<RegisterRequest> req) {
        RegisterRequest body = req.getBody();
        Optional<User> user = userRepository.getUser(body.getUsername());
        if(user.isPresent()) {
            return new MappableHttpResponse<>(req.getVersion(), 400,
                    new HttpHeaders(), new MessageResponse("User already exists"));
        }
        userRepository.insert(new User(body.getUsername(), body.getPassword(), "user"));
        return new MappableHttpResponse<>(req.getVersion(), 200,
                new HttpHeaders(), new MessageResponse("Created"));
    }

}
