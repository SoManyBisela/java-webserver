package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;
import com.simonebasile.sampleapp.service.errors.LoginException;

import java.util.Optional;

public class AuthenticationService {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthenticationService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public void login(LoginRequest req) {
        User user = userRepository.getUser(req.getUsername());
        if(user != null) {
            if(ArgonUtils.verify(req.getPassword(), user.getPassword())) {
                sessionService.updateSession(s -> s.setUsername(user.getUsername()));
            } else {
                throw new LoginException("Username o password non validi");
            }
        } else {
            throw new LoginException("Username o password non validi");
        }
    }

    public void register(RegisterRequest req) {
        User user = userRepository.getUser(req.getUsername());
        if(user != null) {
            throw new LoginException("Username already exists");
        }
        userRepository.insert(new User(req.getUsername(), ArgonUtils.hash(req.getPassword()), "user"));
    }
}
