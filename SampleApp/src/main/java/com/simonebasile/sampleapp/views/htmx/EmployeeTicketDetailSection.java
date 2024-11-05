package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class EmployeeTicketDetailSection extends ElementGroup {
    public EmployeeTicketDetailSection(Ticket ticket, User user) {
        this(ticket, user, null);
    }
    public EmployeeTicketDetailSection(Ticket ticket, User user, String message) {
        final HtmlElement ticketData = div().content(
                div()
                        .attr("class", "ticket-object")
                        .text(ticket.getObject()),
                div()
                        .attr("class", "ticket-message")
                        .text(ticket.getMessage())
        );
        content.add(ticketData);
        final List<Comment> comments = ticket.getComments();
        if(comments != null) {
            for(Comment comment : comments) {
                HtmlElement commentElement = div();
                ticketData.content(commentElement);
                if(comment.getAuthor().equals(user.getUsername())) {
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
        if(ticket.getAssegnee() == null) {
            content.add(
                    button().text("Assign to me")
                            .hxTarget("#main")
                            .hxPost("/ticket")
                            .hxVals("id", ticket.getId(),
                                    "assign", "")
            );
        }
        if(ticket.getState() == TicketState.OPEN && user.getUsername().equals(ticket.getAssegnee())) {
            content.add(
                    button().text("Close")
                            .hxTarget("#main")
                            .hxPost("/ticket")
                            .hxVals("id", ticket.getId(),
                                    "close", "")
            );
        }
        content.add(
                new InputForm()
                        .hxVals("id", ticket.getId())
                        .attr( "id", "add-comment-form")
                        .hxPost("/ticket")
                        .hxTarget("#main")
                        .input("comment", "text")
                        .button(b -> b.text("Send"))
        );
    }
}
