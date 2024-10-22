package com.simonebasile.sampleapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateTicket {
    private String id;
    private String object;
    private String message;
    private String comment;
    private boolean submit;

    public void setSubmit(String submit) {
        this.submit = submit != null && submit.isEmpty();
    }
}
