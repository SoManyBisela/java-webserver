package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class StopWaitingElement extends HtmlElement {
    public StopWaitingElement() {
        super("button");
        attr("class", "abort-button", "ws-send", "true");
        hxVals("type", "STOP_WAITING");
        text("Abort");
    }
}
