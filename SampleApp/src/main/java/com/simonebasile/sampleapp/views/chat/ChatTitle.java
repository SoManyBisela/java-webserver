package com.simonebasile.sampleapp.views.chat;


import com.simonebasile.web.ssr.component.HtmlElement;

public class ChatTitle extends HtmlElement {

    public ChatTitle(String text) {
        super("div");
        attr("class", "chat-title", "id", "chat-title").text(text);
    }
}
