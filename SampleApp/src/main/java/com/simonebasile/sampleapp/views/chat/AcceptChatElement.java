package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class AcceptChatElement extends HtmlElement {
    public AcceptChatElement() {
        super("button");
        text("Accept chat");
        attr("class", "send-message", "ws-send", "true");
        hxVals("type", "ACCEPT_CHAT");
    }
}
