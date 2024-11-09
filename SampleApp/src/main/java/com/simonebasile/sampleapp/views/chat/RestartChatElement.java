package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class RestartChatElement extends HtmlElement {
    public RestartChatElement() {
        super("button");
        text("Close chat");
        attr("class", "send-message", "ws-send", "true");
        hxVals("type", "NEW_CHAT");
    }
}
