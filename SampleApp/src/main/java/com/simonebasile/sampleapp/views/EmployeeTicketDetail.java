package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.InputForm;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class EmployeeTicketDetail extends View {
    public EmployeeTicketDetail(Ticket ticket, User user) {
        this(ticket, user, null);
    }
    public EmployeeTicketDetail(Ticket ticket, User user, String message) {
        addJs("/static/click-actions.js");
        addJs("/static/more-params.js");
        final HtmlElement ticketData = div().content(
                div()
                        .attr("class", "ticket-object")
                        .text(ticket.getObject()),
                div()
                        .attr("class", "ticket-message")
                        .text(ticket.getMessage())
        );
        addContent(ticketData);
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
            addContent(
                    button().text("Assign to me").attr(
                            "form-param-id", ticket.getId(),
                            "form-param-assign", "",
                            "form-action", "/ticket",
                            "form-method", "POST")
            );
        }
        if(ticket.getState() == TicketState.OPEN && user.getUsername().equals(ticket.getAssegnee())) {
            addContent(
                    button().text("Close").attr(
                            "form-param-id", ticket.getId(),
                            "form-param-close", "",
                            "form-action", "/ticket",
                            "form-method", "POST")
            );
        }
        addContent(
                new InputForm()
                        .attr("form-param-id", ticket.getId(),
                                "id", "add-comment-form")
                        .action("/ticket", "POST")
                        .input("comment", "text")
                        .button(b -> b.text("Send"))
        );
    }
}
