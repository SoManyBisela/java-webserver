package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * The section of the chat interface.
 * It connects to the chat server and displays the chat messages.
 */
public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");

        hxExt("ws, simple-loaded-event");
        attr("class", "chatbox closed" ,"ws-connect", "/chat",
                "hx-sle-onload", """
                            htmx.createWebSocket = (url) => {
                                return new WebSocket(url, 'chat');
                            };""").content(
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
