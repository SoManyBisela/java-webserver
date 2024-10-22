package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;
import com.simonebasile.sampleapp.service.errors.UserAuthException;

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
                throw new UserAuthException("Username o password non validi");
            }
        } else {
            throw new UserAuthException("Username o password non validi");
        }
    }

    public void registerUser(RegisterRequest req) {
        register(new User(req.getUsername(), req.getPassword(), Role.user));
    }

    public void register(User req) {
        User user = userRepository.getUser(req.getUsername());
        if(user != null) {
            throw new UserAuthException("Username already exists");
        }
        req.setPassword(ArgonUtils.hash(req.getPassword()));
        userRepository.insert(req);
    }

    public void changePassword(ChangePasswordRequest req) {
        User user = userRepository.getUser(req.getUsername());
        if(ArgonUtils.verify(req.getOldPassword(), user.getPassword())) {
            user.setPassword(ArgonUtils.hash(req.getNewPassword()));
            userRepository.updateUser(user);
        } else {
            throw new UserAuthException("Username o password non validi");
        }
    }
}
