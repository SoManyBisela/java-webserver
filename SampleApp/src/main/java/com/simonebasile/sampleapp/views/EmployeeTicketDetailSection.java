package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;
import com.simonebasile.sampleapp.views.html.custom.*;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class EmployeeTicketDetailSection extends IHtmlElement {

    private final HtmlElement container;
    public EmployeeTicketDetailSection(Ticket ticket, User user) {
        this(ticket, user, null);
    }
    public EmployeeTicketDetailSection(Ticket t, User user, String message) {
        container = div().attr("class", "stack-vertical").content(
                h(1).text("Ticket detail"),
                h(2).attr("class", "ticket-object")
                        .text(t.getObject()),
                h(3).attr("class", "ticket-message")
                        .text(t.getMessage()),
                new AttachmentList(t.getAttachments(), t.getId()),
                userActions(t, user),
                new AddCommentForm(t.getId()),
                new CommentSection(t.getComments(), user.getUsername())
        );
    }

    IHtmlElement userActions(Ticket ticket, User user) {
        if(ticket.getAssignee() == null) {
            return HtmlElement.button().attr("class", "default-button")
                    .text("Assign to me")
                    .hxTarget("#main")
                    .hxPut("/ticket")
                    .hxVals("id", ticket.getId(),
                            "assign", "");
        } else if(ticket.getState() == TicketState.OPEN && user.getUsername().equals(ticket.getAssignee())) {
            return HtmlElement.button().attr("class", "default-button")
                    .text("Close")
                    .hxTarget("#main")
                    .hxPut("/ticket")
                    .hxVals("id", ticket.getId(),
                            "close", "");
        } else {
            return NoElement.instance;
        }
    }

    public EmployeeTicketDetailSection successMessage(String msg) {
        container.content(new SuccessMessage(msg));
        return this;
    }

    public EmployeeTicketDetailSection errorMessage(String msg) {
        container.content(new ErrorMessage(msg));
        return this;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        container.write(os);
    }

}
