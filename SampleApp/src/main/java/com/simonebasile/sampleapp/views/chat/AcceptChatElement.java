package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * A button to accept a chat request.
 */
public class AcceptChatElement extends HtmlElement {
    public AcceptChatElement() {
        super("button");
        hxWsSend();
        attr("class", "accept-chat default-button");
        hxVals("type", "ACCEPT_CHAT");
        content( new MaterialIcon("headset_mic"), span().text("Accept chat"));
    }
}
