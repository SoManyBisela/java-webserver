package com.simonebasile.sampleapp.exceptions;

public class UnacceptableContentTypeException extends RuntimeException{
    public UnacceptableContentTypeException() {
        super("Unacceptable content type");
    }

    public UnacceptableContentTypeException(String receivedContentType) {
        super("Unacceptable content type: " + receivedContentType);
    }
}
