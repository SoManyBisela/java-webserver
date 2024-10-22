package com.simonebasile.sampleapp.model;

import com.simonebasile.sampleapp.dto.CreateTicket;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private String id;
    private String owner;
    private String object;
    private String message;
    private TicketState state;
    private String assegnee;
    private List<Attachment> attachments;
    private List<Comment> comments;

    public Ticket(CreateTicket body) {
        this.message = body.getMessage();
        this.object = body.getObject();
    }
}