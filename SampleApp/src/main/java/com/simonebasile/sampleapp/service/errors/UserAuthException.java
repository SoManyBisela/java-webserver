package com.simonebasile.sampleapp.service.errors;

public class UserAuthException extends RuntimeException {
    public UserAuthException(String message) {
        super(message);
    }
}
