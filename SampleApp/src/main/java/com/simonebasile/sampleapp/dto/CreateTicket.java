package com.simonebasile.sampleapp.dto;

import com.simonebasile.sampleapp.model.Attachment;

import java.util.List;

public class CreateTicket {
    private String object;
    private String message;

    public CreateTicket() { }
    public CreateTicket(String id, String owner, String object, String message, String state, String employee, List<Attachment> attachments) {
        this.object = object;
        this.message = message;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
