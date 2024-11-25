package com.simonebasile.sampleapp.views.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

/**
 * Represents a form to add a comment to a ticket.
 */
public class AddCommentForm extends HtmlElement {
    public AddCommentForm(String id) {
        super("form");
        attr( "id", "add-comment-form")
                .hxPut("/ticket")
                .hxTarget("#main")
                .hxVals("id", id)
                .content(
                        div().attr("class", "add-comment")
                                .content(
                                        new TextInputElement("comment", "Comment").style("flex-grow: 1"),
                                        button().attr("style", "margin-top: 0.4rem",
                                                "class", "default-button",
                                                "type", "submit").content(new MaterialIcon("send"))
                                )
                );
    }
}
