package com.simonebasile.sampleapp.views.chat;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.custom.MaterialIcon;

/**
 * The form with the input field to send a message.
 */
public class SendMessageElement extends HtmlElement {
    private HtmlElement input;
    private HtmlElement button;
    private final static String autosize = "this.style.height = ''; this.style.height = `min(${this.scrollHeight}px, calc(5rem + 12px))`";
    public SendMessageElement() {
        super("form");
        attr("class", "send-message", "id", "send-message", "ws-send", "true");
        hxVals("type", "SEND_MESSAGE");
        input = textarea().attr("type", "text",
                        "rows", "1",
                        "name", "sval",
                        "placeholder", "Type a message...",
                        "hx-sle-onload", autosize,
                        "oninput", autosize,
                        "onkeypress", "if(event.key === 'Enter' && !event.shiftKey) {event.preventDefault(); this.parentElement.querySelector('button[type=\"submit\"]').click()}"
                        )
                .hxExt("simple-loaded-event,debug");
        content(
                input,
                div().attr("class", "send-btn-container").content(
                        button = button().content(new MaterialIcon("send")).attr("type", "submit", "class", "button-icon")
                )
        );
    }

    /**
     * Calling this method will focus the input field when the page is loaded.
     * @return this
     */
    public SendMessageElement focusOnLoad() {
        input.attr("hx-sle-onload", autosize + ";this.focus()");
        return this;
    }

    /**
     * Calling this method disable the inputs, the sending of the form, will show a
     * message telling the user that the chat is disconnected
     * @return this
     */
    public SendMessageElement disconnected() {
        attr("ws-send", "false");
        button.attr("disabled", "true");
        input.attr("disabled", "true", "placeholder", "chat disconnected");
        content(div().attr("class", "input-cover"));
        return this;
    }
}
