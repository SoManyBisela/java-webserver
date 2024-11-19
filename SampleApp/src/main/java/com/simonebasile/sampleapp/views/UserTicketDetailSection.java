package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Attachment;
import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;
import com.simonebasile.sampleapp.views.html.custom.*;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketDetailSection extends IHtmlElement {
    private final HtmlElement container;
    private HtmlElement messageTarget;

    private static boolean ticketExists(Ticket t) {
        return t != null && t.getId() != null;
    }
    public UserTicketDetailSection(Ticket ticket) {
        if(!ticketExists(ticket) || ticket.getState() == TicketState.DRAFT) {
            container = draftTicket(ticket);
        } else {
            container = ticket(ticket);
        }
    }

    HtmlElement draftTicket(Ticket t) {
        String object = "";
        String message = "";
        if(t != null) {
            message = t.getMessage();
            object = t.getObject();
        }
        String formId = IdGenerator.get();
        final boolean createTicket = t != null && t.getId() != null;
        var editForm = createTicket ? form().hxPut("/ticket").hxVals("id", t.getId()) : form().hxPost("/ticket");
        return messageTarget = div().attr("class", "stack-vertical")
                .content(
                        h(1).text(createTicket ? "Modifica ticket" : "Crea ticket"),
                        editForm
                                .attr("id", formId)
                                .hxTarget("#main")
                                .content(
                                        div().attr("class", "stack-vertical")
                                                .content(
                                                        new TextInputElement("object", "Object").value(object),
                                                        new TextAreaElement("message", "Message").value(message)
                                                )
                                ),
                        createTicket ? new ElementGroup(
                                new AttachmentList(t.getAttachments(), t.getId()),
                                uploadAttachment(t.getId())
                        ) : NoElement.instance,
                        div().attr("class", "stack-horizontal child-grow").content(
                                createTicket ? button().attr("class", "default-button")
                                        .text("Submit").attr("type", "submit", "name", "submit", "form", formId) : NoElement.instance,
                                button().attr("class", "default-button")
                                        .text("Save as draft").attr("type", "submit", "form", formId)
                        )
                );
    }

    private HtmlElement uploadAttachment(String id) {
        return form().attr("class", "upload-attachment",
                        "hx-raw-file-param", "filecontent",
                        "hx-raw-filename-param", "filename")
                .hxExt("body-file")
                .hxPost("/attachment")
                .hxVals("ticketId", id)
                .hxTarget("#main")
                .hxSwap("innerHTML")
                .content(
                        div().attr("class", "stack-horizontal").content(
                                input().attr("type", "file", "name", "filecontent"),
                                button().attr("class", "default-button").text("Upload").attr("class", "upload-button", "type", "submit")
                        )
                );
    }

    HtmlElement ticket(Ticket t) {
        return messageTarget = div().attr("class", "stack-vertical").content(
                h(1).text("Ticket detail"),
                h(2).attr("class", "ticket-object")
                        .text(t.getObject()),
                h(3).attr("class", "ticket-message")
                        .text(t.getMessage()),
                new AttachmentList(t.getAttachments(), t.getId()),
                new AddCommentForm(t.getId()),
                new CommentSection(t.getComments(), t.getOwner())
        );
    }

    public UserTicketDetailSection successMessage(String msg) {
        messageTarget.content(new SuccessMessage(msg));
        return this;
    }

    public UserTicketDetailSection errorMessage(String msg) {
        messageTarget.content(new ErrorMessage(msg));
        return this;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        container.write(os);
    }
}
