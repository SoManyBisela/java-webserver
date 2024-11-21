package com.simonebasile.sampleapp;

import com.simonebasile.sampleapp.model.Attachment;
import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestUtils {

    public static Ticket mkTicket(String id, String object, String message, TicketState state, String owner) {
        final Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setObject(object);
        ticket.setMessage(message);
        ticket.setState(state);
        ticket.setOwner(owner);
        var attachments = new ArrayList<Attachment>();
        attachments.add(new Attachment("path", "name"));
        attachments.add(new Attachment("path2", "name2"));
        ticket.setAttachments(attachments);
        var comments = new ArrayList<Comment>();
        comments.add(new Comment("author", "message", LocalDateTime.now()));
        comments.add(new Comment("author2", "message2", LocalDateTime.now()));
        ticket.setComments(comments);
        return ticket;
    }

}
