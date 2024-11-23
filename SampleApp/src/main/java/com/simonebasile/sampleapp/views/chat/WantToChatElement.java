package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

/**
 * A button to start a chat.
 */
public class WantToChatElement extends HtmlElement {
    public WantToChatElement() {
        super("button");
        text("Begin chat");
        attr("class", "send-message", "ws-send", "true");
        hxVals("type", "WANT_TO_CHAT");
    }
}
