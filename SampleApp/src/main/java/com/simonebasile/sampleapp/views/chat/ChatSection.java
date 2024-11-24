package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

/**
 * The section of the chat interface.
 * It connects to the chat server and displays the chat messages.
 */
public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");
        attr("class", "chatbox closed").content(
                div().attr("class", "chatbox-content").content(
                        div().attr("class", "chat-header").content(
                                chatboxToggle().content(new MaterialIcon("minimize"))
                        ),
                        div().attr("class", "chat-section",
                                        "id", "chat-section",
                                        "ws-connect", "/chat")
                                .hxExt("ws, debug")
                                .content(div().attr("id", "chat-container")
                                        .text("connecting..."))
                ),
                chatboxToggle().content(new MaterialIcon("contact_support"))
        );
    }

    private HtmlElement chatboxToggle() {
        return button().attr("class", "chatbox-toggle button-icon", "onclick", "htmx.closest(this, '.chatbox').classList.toggle('closed')");
    }
}
