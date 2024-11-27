package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.sampleapp.views.html.HtmlElement;

/**
 * A button to start a new chat after the previous one has ended.
 */
public class RestartChatElement extends HtmlElement {
    public RestartChatElement() {
        super("button");
        hxWsSend();
        attr("class", "delete-button", "id", "close-chat-button");
        hxVals("type", "NEW_CHAT");
        content(new MaterialIcon("autorenew"));
    }
}
