package com.simonebasile.sampleapp.views.chat;


import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * A section to inform the user that they are already connected to the chat system.
 */
public class AlreadyConnectedSection extends HtmlElement {
    public AlreadyConnectedSection() {
        super("div");
        attr(
                "class", "chat-section"
        );
        content(div().text("You are already connected to the chat system from another window"));
    }
}
