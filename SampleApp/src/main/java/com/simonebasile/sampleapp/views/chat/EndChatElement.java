package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class EndChatElement extends HtmlElement {
    public EndChatElement() {
        super("button");
        text("Close chat");
        attr("class", "send-message", "ws-send", "true");
        hxVals("type", "END_CHAT");
    }
}