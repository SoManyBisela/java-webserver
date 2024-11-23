package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

/**
 * A button to start a new chat after the previous one has ended.
 */
public class RestartChatElement extends HtmlElement {
    public RestartChatElement() {
        super("button");
        text("Close chat");
        attr("class", "send-message", "ws-send", "true");
        hxVals("type", "NEW_CHAT");
    }
}
