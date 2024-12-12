package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.custom.MaterialIcon;
import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * A button to stop waiting for a chat request.
 */
public class StopWaitingElement extends HtmlElement {
    public StopWaitingElement() {
        super("button");
        attr("class", "abort-button default-button", "ws-send", "true");
        hxVals("type", "STOP_WAITING");
        content( new MaterialIcon("block"), span().text("Abort"));
    }
}
