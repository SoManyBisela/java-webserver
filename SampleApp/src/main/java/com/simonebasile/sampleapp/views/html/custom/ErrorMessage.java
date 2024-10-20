package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

public class ErrorMessage extends HtmlElement {
    public ErrorMessage(String text) {
        super("div");
        attr("class", "error-msg");
        text(text);
    }
}
