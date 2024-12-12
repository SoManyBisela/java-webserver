package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * A button to end the chat.
 */
public class EndChatElement extends HtmlElement {
    public EndChatElement() {
        super("button");
        hxWsSend();
        hxExt("debug");
        hxVals("type", "END_CHAT");
        attr("class", "delete-button", "id", "close-chat-button");
        content(new MaterialIcon("close"));
    }

    public EndChatElement hidden() {
        return (EndChatElement) attr("hidden", "true");
    }
}
