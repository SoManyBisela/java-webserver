package com.simonebasile.sampleapp.service;

import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.repository.UserRepository;

import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getUser(String username) {
        return userRepository.getUser(username);
    }
}
