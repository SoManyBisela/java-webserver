package com.simonebasile.sampleapp.service.errors;

public class UpdateTicketException extends RuntimeException {
    public UpdateTicketException(String message) {
        super(message);
    }
}
