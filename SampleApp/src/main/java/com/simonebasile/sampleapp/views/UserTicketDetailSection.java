package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Attachment;
import com.simonebasile.sampleapp.model.Comment;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.TicketState;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.InputForm;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
                        .button( b -> b.text("Save as draft")),
                attachmentList(t.getAttachments(), t.getId())
                        .content(uploadAttachment(t.getId()))
        );
    }

    private HtmlElement uploadAttachment(String id) {
        return div().attr("class", "upload-attachment").content(
                new HtmlElement("script").content(new IHtmlElement() {
                    @Override
                    public void write(OutputStream out) throws IOException {
                        out.write("""
                        function upload(fileinput, id) {
                            console.log(fileinput, id);
                            let file = fileinput.files[0];
                            let filename = file.name;
                            fetch(`/attachment?ticketId=${id}&filename=${encodeURIComponent(filename)}`, {
                                method: "POST",
                                body: fileinput.files[0]
                            })
                            .then(r => r.text())
                            .then(content => htmx.swap("#main", content, {swapStyle: 'innerHTML'}));
                        }""".getBytes(StandardCharsets.UTF_8));
                    }
                }),
                input().attr("type", "file"),
                button().text("Upload").attr("class",
                        "upload-button", "onclick", "upload(this.previousElementSibling, '" + id + "')")
        );
    }


    HtmlElement ticket(Ticket t) {
        HtmlElement ticketData = div().content(
                div()
                        .attr("class", "ticket-object")
                        .text(t.getObject()),
                div()
                        .attr("class", "ticket-message")
                        .text(t.getMessage())
        );

        ticketData.content(
                commentSection(ticketData, t.getComments(), t.getOwner(), t.getId()),
                attachmentList(t.getAttachments(), t.getId())
        );

        return ticketData;
    }

    private HtmlElement attachmentList(List<Attachment> attachments, String id) {
        HtmlElement container = table().attr("class", "attachments");
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
                new InputForm()
                        .attr( "id", "add-comment-form")
                        .hxPost("/ticket")
                        .hxTarget("#main")
                        .hxVals("id", ticketId)
                        .input("comment", "text")
                        .button(b -> b.text("Send"))
        );
        return commentSection;
    }

    public UserTicketDetailSection errorMessage(String msg) {
        this.content.add(new ErrorMessage(msg));
        return this;
    }

}
