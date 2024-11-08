package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class SendMessageElement extends HtmlElement {
    public SendMessageElement() {
        super("form");
        attr("class", "send-message", "id", "send-message", "ws-send", "true");
        hxVals("type", "SEND_MESSAGE");
        content(
                input().attr("type", "text", "name", "sval"),
                button().text("Send").attr("type", "submit")
        );
    }
}
