package com.simonebasile.sampleapp.service.errors;

public class CreateTicketException extends RuntimeException {
    public CreateTicketException(String message) {
        super(message);
    }
}
