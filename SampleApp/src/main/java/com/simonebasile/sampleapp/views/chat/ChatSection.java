package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.custom.MaterialIcon;

/**
 * The section of the chat interface.
 * It connects to the chat server and displays the chat messages.
 */
public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");

        hxExt("ws");
        attr("class", "chatbox closed" ,"ws-connect", "/chat").content(
                div().attr("class", "chatbox-content").content(
                        div().attr("class", "chat-header").content(
                                new ChatTitle("Chat"),
                                chatboxToggle("button-icon").content(new MaterialIcon("minimize", "lift")),
                                new EndChatElement().hidden()
                        ),
                        div().attr("class", "chat-section",
                                        "id", "chat-section")
                                .content(div().attr("id", "chat-container")
                                        .text("connecting..."))
                ),
                chatboxToggle("default-button").content(new MaterialIcon("contact_support"))
        );
    }

    private HtmlElement chatboxToggle(String addClass) {
        return button().attr("class", "chatbox-toggle " + addClass, "onclick", "htmx.closest(this, '.chatbox').classList.toggle('closed')");
    }
}
