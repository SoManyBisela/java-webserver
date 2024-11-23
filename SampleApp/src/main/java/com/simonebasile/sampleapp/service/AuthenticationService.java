package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.ChangePasswordRequest;
import com.simonebasile.sampleapp.dto.CreateUserRequest;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.repository.UserRepository;
import com.simonebasile.sampleapp.dto.LoginRequest;
import com.simonebasile.sampleapp.dto.RegisterRequest;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.security.ArgonUtils;
import com.simonebasile.sampleapp.service.errors.UserAuthException;

/**
 * Service to handle user authentication.
 */
public class AuthenticationService {
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthenticationService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    /**
     * Logs in a user.
     * saves the username in the session.
     * @param sessionId the session id
     * @param req the login request
     */
    public void login(String sessionId, LoginRequest req) {
        User user = userRepository.getUser(req.getUsername());
        if(user != null) {
            if(ArgonUtils.verify(req.getPassword(), user.getPassword())) {
                sessionService.updateSession(sessionId, s -> s.setUsername(user.getUsername()));
            } else {
                throw new UserAuthException("Invalid username or password");
            }
        } else {
            throw new UserAuthException("Invalid username or password");
        }
    }

    /**
     * Registers a new user.
     * delegates the creation of the user to the {@link CreateUserRequest} class.
     * @param req the register request
     */
    public void registerUser(RegisterRequest req) {
        register(new CreateUserRequest(req));
    }

    /**
     * Registers a new user.
     * tests the validity of the request and the uniqueness of the username.
     * @param req the create user request
     */
    public void register(CreateUserRequest req) {
        if(Utils.isEmpty(req.getUsername())) {
            throw new UserAuthException("Invalid username");
        }
        if(Utils.isEmpty(req.getPassword())) {
            throw new UserAuthException("Invalid password");
        }
        if(!req.getPassword().equals(req.getCpassword())) {
            throw new UserAuthException("Passwords do not match");
        }
        User user = userRepository.getUser(req.getUsername());
        if(user != null) {
            throw new UserAuthException("Username already exists");
        }
        req.setPassword(ArgonUtils.hash(req.getPassword()));
        userRepository.insert(new User(req.getUsername(), req.getPassword(), req.getRole()));
    }

    /**
     * Changes the password of a user.
     * tests the validity of the request and the correctness of the old password.
     * @param req the change password request
     */
    public void changePassword(ChangePasswordRequest req) {
        if(Utils.isEmpty(req.getNewPassword())) {
            throw new UserAuthException("Invalid password");
        }
        if(!req.getNewPassword().equals(req.getConPassword())) {
            throw new UserAuthException("Passwords do not match");
        }
        User user = userRepository.getUser(req.getUsername());
        if(ArgonUtils.verify(req.getOldPassword(), user.getPassword())) {
            user.setPassword(ArgonUtils.hash(req.getNewPassword()));
            userRepository.updateUser(user);
        } else {
            throw new UserAuthException("Wrong password");
        }
    }
}
