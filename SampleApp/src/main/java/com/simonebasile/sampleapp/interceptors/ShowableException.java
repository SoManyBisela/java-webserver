package com.simonebasile.sampleapp.interceptors;

/**
 * An exception that can be shown to the user.
 * Wrapping an exception with this class will show a toast to the user with the exception message.
 * Useful to decide whether the user can be shown the exception message or not.
 */
public class ShowableException extends RuntimeException{
    public ShowableException(String message) {
        super(message);
    }
    public ShowableException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
