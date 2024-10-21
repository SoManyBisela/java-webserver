package com.simonebasile.sampleapp.model;

import com.simonebasile.sampleapp.dto.CreateTicket;

import java.util.List;

public class Ticket {
    private String id;
    private String owner;
    private String object;
    private String message;
    private TicketState state;
    private String employee;
    private List<Attachment> attachments;

    public Ticket() { }
    public Ticket(String id, String owner, String object, String message, TicketState state, String employee, List<Attachment> attachments) {
        this.id = id;
        this.owner = owner;
        this.object = object;
        this.message = message;
        this.state = state;
        this.employee = employee;
        this.attachments = attachments;
    }

    public Ticket(CreateTicket body) {
        this.message = body.getMessage();
        this.object = body.getObject();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
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

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }
}
