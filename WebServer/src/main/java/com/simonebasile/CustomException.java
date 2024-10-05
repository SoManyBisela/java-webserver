package com.simonebasile;

public class CustomException extends RuntimeException {
    public CustomException(String s) {
        super(s);
    }

    public CustomException(String s, Throwable cause) {
        super(s, cause);
    }
}
