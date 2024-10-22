package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.InputForm;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketDetail extends View {
    public UserTicketDetail(Ticket ticket) {
        this(ticket, null);
    }

    public UserTicketDetail(Ticket ticket, String errorMessage) {
        url("/ticket?id=" + ticket.getId());
        addJs("/static/more-params.js");
        addContent(
                a().text("Back to tickets").attr("href", "/tickets"),
                ticket.getState() == TicketState.DRAFT ?
                        draftTicket(ticket) :
                        ticket(ticket)
        );
    }

    HtmlElement draftTicket(Ticket t) {
        return div().content(
                new InputForm().action("/ticket", "POST")
                        .attr("form-param-id", t.getId())
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
                        .attr("form-param-id", t.getId(),
                                "id", "add-comment-form")
                        .action("/ticket", "POST")
                        .input("comment", "text")
                        .button(b -> b.text("Send"))
        );
        return ticketData;
    }

}
