package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

/**
 * The section of the chat interface.
 * It connects to the chat server and displays the chat messages.
 */
public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");
        attr(
                "class", "chat-section",
                "id", "chat-section",
                "ws-connect", "/chat"
        );
        hxExt("ws, debug");
        content(
                div().attr( "id", "chat-container" ).text("connecting...")
        );
    }
}
