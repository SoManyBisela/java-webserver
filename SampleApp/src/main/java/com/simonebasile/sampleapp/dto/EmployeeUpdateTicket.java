package com.simonebasile.sampleapp.dto;

public class EmployeeUpdateTicket {
    private String id;
    private String comment;
    private boolean close;

    public EmployeeUpdateTicket() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isClose() {
        return close;
    }

    public void setClose(boolean close) {
        this.close = close;
    }
}
