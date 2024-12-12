package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.views.custom.*;
import com.simonebasile.web.ssr.component.ElementGroup;
import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.component.IHtmlElement;
import com.simonebasile.web.ssr.component.NoElement;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.web.ssr.component.HtmlElement.*;
/**
 * Represents the section of the page that shows the details of a ticket for a user.
 * It allows the user to create a new ticket or edit an existing one that is in draft state.
 */
public class UserTicketDetailSection implements IHtmlElement {
    private final HtmlElement container;

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
        return div().attr("class", "stack-vertical")
                .content(
                        h(1).text(createTicket ? "Modifica ticket" : "Crea ticket"),
                        editForm
                                .attr("id", formId)
                                .hxTarget("#main")
                                .content(
                                        div().attr("class", "stack-vertical")
                                                .content(
                                                        new TextInputElement("object", "Object").value(object).disableAutoSubmit(),
                                                        new TextAreaElement("message", "Message").value(message)
                                                )
                                ),
                        createTicket ? new ElementGroup(
                                new AttachmentList(t.getAttachments(), t.getId()),
                                uploadAttachment(t.getId())
                        ) : NoElement.instance,
                        div().attr("class", "form-button").content(
                                createTicket ? button().attr("class", "default-button")
                                        .content(new MaterialIcon("ios_share"), span().text("Submit"))
                                        .attr("type", "submit", "name", "submit", "form", formId) : NoElement.instance,
                                button().attr("class", "default-button")
                                        .content(new MaterialIcon("save"), span().text("Save as draft"))
                                        .attr("type", "submit", "form", formId)
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
                .hxTarget("#attachmentlist")
                .hxSwap("innerHTML")
                .content(
                        div().attr("class", "stack-horizontal").content(
                                input().attr("type", "file", "name", "filecontent",
                                        "onchange", "this.setCustomValidity('')",
                                        "hx-on::validation:validate", """
                                            if(!this.files[0]) {
                                                this.setCustomValidity('Please select a file');
                                                htmx.closest(this, 'form').reportValidity();
                                            } else {
                                                this.setCustomValidity('');
                                            }
                                            """
                                ),
                                button().attr("class", "default-button").text("Upload").attr("class", "upload-button", "type", "submit")
                        )
                );
    }

    HtmlElement ticket(Ticket t) {
        return div().attr("class", "stack-vertical").content(
                h(1).text("Ticket detail"),
                new TextInputElement("object", "Object").value(t.getObject()).readonly(),
                new TextAreaElement("message", "Message").value(t.getMessage()).readonly(),
                new AttachmentList(t.getAttachments(), t.getId()),
                new ElementGroup(
                        t.getState() == TicketState.CLOSED ? NoElement.instance : new AddCommentForm(t.getId()),
                        new CommentSection(t.getComments(), t.getOwner())
                )
        );
    }

    @Override
    public void write(OutputStream os) throws IOException {
        container.write(os);
    }
}
