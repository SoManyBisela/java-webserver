package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.custom.MaterialIcon;

/**
 * A button to start a chat.
 */
public class WantToChatElement extends HtmlElement {
    public WantToChatElement() {
        super("button");
        hxWsSend();
        attr("class", "default-button begin-chat");
        hxVals("type", "WANT_TO_CHAT");
        content( new MaterialIcon("headset_mic"), span().text("Begin chat"));
    }
}
