package com.simonebasile.sampleapp.dto;

public class UserUpdateTicket {
    private String id;
    private String object;
    private String message;
    private String comment;
    private boolean submit;

    public UserUpdateTicket() { }
    public UserUpdateTicket(String id, String object, String message, boolean submit) {
        this.id = id;
        this.object = object;
        this.message = message;
        this.submit = submit;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
