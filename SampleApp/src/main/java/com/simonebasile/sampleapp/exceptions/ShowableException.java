package com.simonebasile.sampleapp.exceptions;

public class ShowableException extends RuntimeException{
    public ShowableException(String message) {
        super(message);
    }
    public ShowableException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
