package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class SendMessageElement extends HtmlElement {
    private HtmlElement input;
    public SendMessageElement() {
        super("form");
        attr("class", "send-message", "id", "send-message", "ws-send", "true");
        hxExt("simple-loaded-event,debug");
        hxVals("type", "SEND_MESSAGE");
        input = input().attr("type", "text", "name", "sval");
        content(
                input,
                button().text("Send").attr("type", "submit")
        );
    }

    public SendMessageElement focusOnLoad() {
        input.attr("hx-sle-onload", "this.focus()");
        return this;
    }
}
