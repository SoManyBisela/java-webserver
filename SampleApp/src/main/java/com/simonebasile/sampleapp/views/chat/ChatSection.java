package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class ChatSection extends HtmlElement {
    public ChatSection() {
        super("div");
        attr(
                "class", "chat-section",
                "id", "class-section",
                "ws-connect", "/chat"
        );
        content(div().attr( "id", "chatcontainer" ).text("connecting..."));
    }
}
