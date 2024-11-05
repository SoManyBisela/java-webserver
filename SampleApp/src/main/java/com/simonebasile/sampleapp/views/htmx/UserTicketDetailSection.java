package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketDetailSection extends ElementGroup {
    public UserTicketDetailSection(Ticket ticket) {
        this(ticket, null);
    }

    public UserTicketDetailSection(Ticket ticket, String errorMessage) {
        content.add( ticket.getState() == TicketState.DRAFT ?
                        draftTicket(ticket) :
                        ticket(ticket)
        );
    }

    HtmlElement draftTicket(Ticket t) {
        return div().content(
                new InputForm().hxPost("/ticket")
                        .hxVals("id", t.getId())
                        .hxTarget("#main")
                        .input("object", "text", a -> a.input().attr("value", t.getObject(), "class", "ticket-object"))
                        .input("message", "text", a -> a.input().attr("value", t.getMessage(), "class", "ticket-message"))
                        .button( b -> b.text("Submit").attr("name", "submit"))
                        .button( b -> b.text("Save as draft"))
        );
    }

    HtmlElement ticket(Ticket t) {
        final HtmlElement ticketData = div().content(
                div()
                        .attr("class", "ticket-object")
                        .text(t.getObject()),
                div()
                        .attr("class", "ticket-message")
                        .text(t.getMessage())
        );
        final List<Comment> comments = t.getComments();
        if(comments != null) {
            for(Comment comment : comments) {
                HtmlElement commentElement = div();
                ticketData.content(commentElement);
                if(comment.getAuthor().equals(t.getOwner())) {
                    commentElement.attr("class", "ticket-comment-owner")
                            .content(
                                    p().text(comment.getContent())
                            );
                } else {
                    commentElement.attr("class", "ticket-comment-other")
                            .content(
                                    p().content(span().text(comment.getAuthor() + ": "))
                                            .text(comment.getContent())
                            );
                }
            }
        }
        ticketData.content(
                new InputForm()
                        .attr( "id", "add-comment-form")
                        .hxPost("/ticket")
                        .hxTarget("#main")
                        .hxVals("id", t.getId())
                        .input("comment", "text")
                        .button(b -> b.text("Send"))
        );
        return ticketData;
    }

}
