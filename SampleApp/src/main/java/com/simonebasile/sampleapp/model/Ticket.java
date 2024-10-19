package com.simonebasile.sampleapp.model;

import java.util.List;

public class Ticket {
    private String owner;
    private String message;
    private List<Attachment> attachments;

    public Ticket() { }
    public Ticket(String owner, String message, List<Attachment> attachments) {
        this.owner = owner;
        this.message = message;
        this.attachments = attachments;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
