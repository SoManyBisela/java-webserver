package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.repository.UserRepository;

import java.util.Optional;

/**
 * Service for accessing users.
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Gets a user by username.
     * @param username the username
     * @return the user
     */
    public User getUser(String username) {
        return userRepository.getUser(username);
    }
}
