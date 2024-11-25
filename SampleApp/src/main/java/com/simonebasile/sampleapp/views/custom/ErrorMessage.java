package com.simonebasile.sampleapp.views.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

/**
 * Represents an error message in an HTML page.
 */
public class ErrorMessage extends HtmlElement {
    public ErrorMessage(String text) {
        super("div");
        attr("class", "error-msg");
        text(text);
    }
}
