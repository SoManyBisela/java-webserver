package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.dto.ChatProtoMessage;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

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
