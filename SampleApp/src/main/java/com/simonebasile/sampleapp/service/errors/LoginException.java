package com.simonebasile.sampleapp.service.errors;

public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
