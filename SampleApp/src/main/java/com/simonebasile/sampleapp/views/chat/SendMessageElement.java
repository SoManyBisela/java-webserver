package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

public class SendMessageElement extends HtmlElement {
    private HtmlElement input;
    public SendMessageElement() {
        super("form");
        attr("class", "send-message", "id", "send-message", "ws-send", "true");

        hxVals("type", "SEND_MESSAGE");
        input = input().attr("type", "text", "name", "sval").
                hxExt("simple-loaded-event,debug");
        content(
                input,
                button().content(new MaterialIcon("send")).attr("type", "submit")
        );
    }

    public SendMessageElement focusOnLoad() {
        input.attr("hx-sle-onload", "this.focus()");
        return this;
    }
}
