package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");
        attr(
                "class", "chat-section",
                "id", "chat-section",
                "ws-connect", "/chatroom"
        );
        hxExt("debug");
        hxExt("ws");
        content(
                div().attr( "id", "chat-container" ).text("connecting...")
        );
    }
}
