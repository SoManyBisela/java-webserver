package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;

import java.util.Optional;

public class AuthenticationService {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthenticationService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public boolean login(LoginRequest req) {
        return userRepository.getUser(req.getUsername()).map(u -> {
            if(ArgonUtils.verify(req.getPassword(), u.getPassword())) {
                sessionService.currentSession().setUsername(u.getUsername());
                return true;
            } else {
                return false;
            }
        }).orElse(false);
    }

    public boolean register(RegisterRequest req) {
        Optional<User> user = userRepository.getUser(req.getUsername());
        if(user.isPresent()) {
            return false;
        }
        userRepository.insert(new User(req.getUsername(), req.getPassword(), "user"));
        return true;
    }
}
