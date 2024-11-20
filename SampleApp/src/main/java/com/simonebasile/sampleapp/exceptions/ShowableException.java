package com.simonebasile.sampleapp.exceptions;

public class ShowableException extends RuntimeException{
    public ShowableException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
