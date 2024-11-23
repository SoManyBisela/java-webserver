package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

/**
 * The form with the input field to send a message.
 */
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

    /**
     * Calling this method will focus the input field when the page is loaded.
     * @return this
     */
    public SendMessageElement focusOnLoad() {
        input.attr("hx-sle-onload", "this.focus()");
        return this;
    }
}
