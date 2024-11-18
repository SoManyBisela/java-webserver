package com.simonebasile.sampleapp.views;

import com.mongodb.internal.connection.tlschannel.NeedsReadException;
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
    public UserTicketDetailSection(Ticket ticket) {
        content.add( ticket == null || ticket.getState() == TicketState.DRAFT ?
                draftTicket(ticket) :
                ticket(ticket)
        );
    }

    HtmlElement draftTicket(Ticket t) {
        String object = "";
        String message = "";
        if(t != null) {
            object = t.getObject();
            message = t.getMessage();
        }
        String formId = IdGenerator.get();
        var editForm = t == null ? form().hxPost("/ticket") : form().hxPut("/ticket").hxVals("id", t.getId());
        return div().attr("class", "stack-vertical")
                .content(
                        h(1).text(t == null ? "Crea ticket" : "Modifica ticket"),
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
                        t == null ?
                                NoElement.instance :
                                new ElementGroup(
                                        attachmentList(t.getAttachments(), t.getId()),
                                        uploadAttachment(t.getId())
                                ),
                        t == null ? NoElement.instance : button().text("Submit").attr("type", "submit", "name", "submit", "form", formId),
                        button().text("Save as draft").attr("type", "submit", "form", formId)
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
                                button().text("Upload").attr("class", "upload-button", "type", "submit")
                        )
                );
    }


    HtmlElement ticket(Ticket t) {
        HtmlElement ticketData = div().content(
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

    private HtmlElement attachmentList(List<Attachment> attachments, String id) {
        HtmlElement container = table().attr("class", "attachments", "id", "attachmentlist");
        if(attachments != null) {
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
                                                new TextInputElement("comment", "Comment")),
                                button().attr("type", "submit").text("Send")
                        )
        );
        return commentSection;
    }

    public UserTicketDetailSection errorMessage(String msg) {
        this.content.add(new ErrorMessage(msg));
        return this;
    }

}
