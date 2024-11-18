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

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketDetailSection extends ElementGroup {
    private HtmlElement container;

    private static boolean ticketExists(Ticket t) {
        return t != null && t.getId() != null;
    }
    public UserTicketDetailSection(Ticket ticket) {
        content.add( !ticketExists(ticket) || ticket.getState() == TicketState.DRAFT ?
                draftTicket(ticket) :
                ticket(ticket)
        );
    }

    HtmlElement draftTicket(Ticket t) {
        String object = "";
        String message = "";
        if(t != null) {
            message = t.getMessage();
            object = t.getObject();
        }
        String formId = IdGenerator.get();
        final boolean createTicket = ticketExists(t);
        var editForm = createTicket ? form().hxPut("/ticket").hxVals("id", t.getId()) : form().hxPost("/ticket");
        return container = div().attr("class", "stack-vertical")
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
                                attachmentList(t.getAttachments(), t.getId()),
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
        HtmlElement ticketData = container = div().content(
                div().attr("class", "ticket-object")
                        .text(t.getObject()),
                div().attr("class", "ticket-message")
                        .text(t.getMessage())
        );

        ticketData.content(
                commentSection(ticketData, t.getComments(), t.getOwner(), t.getId()),
                attachmentList(t.getAttachments(), t.getId())
        );

        return ticketData;
    }

    private IHtmlElement attachmentList(List<Attachment> attachments, String id) {
        if(attachments == null || attachments.isEmpty()) return NoElement.instance;
        HtmlElement container = table().attr("class", "attachments", "id", "attachmentlist");
        for (int i = 0; i < attachments.size(); i++) {
            Attachment attachment = attachments.get(i);
            container.content(tr().content(
                    td().text(attachment.getName()),
                    td().content(a().attr(
                            "href", "/attachment?ticketId=" + id + "&ati=" + i,
                            "target", "_blank"
                    ).text("Download"))
            ));
        }
        return container;

    }

    private HtmlElement commentSection(HtmlElement ticketData, List<Comment> comments, String owner, String ticketId) {
        HtmlElement commentSection = div().attr("class", "comments");
        if(comments != null && !comments.isEmpty()) {
            for(Comment comment : comments) {
                HtmlElement commentElement = div();
                commentSection.content(commentElement);
                if(comment.getAuthor().equals(owner)) {
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
                form()
                        .attr( "id", "add-comment-form")
                        .hxPut("/ticket")
                        .hxTarget("#main")
                        .hxVals("id", ticketId)
                        .content(
                                div().attr("class", "stack-horizontal")
                                        .content(
                                                new TextInputElement("comment", "Comment").style("flex-grow: 1"),
                                                button().attr("style", "margin-top: 0.4rem",
                                                        "class", "default-button",
                                                        "type", "submit").text("Send")
                                        )
                        )
        );
        return commentSection;
    }

    public UserTicketDetailSection successMessage(String msg) {
        container.content(new SuccessMessage(msg));
        return this;
    }

    public UserTicketDetailSection errorMessage(String msg) {
        container.content(new ErrorMessage(msg));
        return this;
    }

}
