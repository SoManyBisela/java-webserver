package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");
        attr(
                "class", "chat-section",
                "id", "class-section",
                "ws-connect", "/chatroom"
        );
        hxExt("debug");
        hxExt("ws");
        content(div().attr( "id", "chatcontainer" ).text("connecting..."));
    }
}
