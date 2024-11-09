package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class AlreadyConnectedSection extends HtmlElement {
    public AlreadyConnectedSection() {
        super("div");
        attr(
                "class", "chat-section"
        );
        content(div().text("You are already connected to the chat system from another window"));
    }
}
