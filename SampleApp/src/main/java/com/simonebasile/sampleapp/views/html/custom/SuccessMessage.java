package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class SuccessMessage extends HtmlElement {
    public SuccessMessage(String text) {
        super("div");
        attr("class", "success-msg");
        text(text);
    }
}
