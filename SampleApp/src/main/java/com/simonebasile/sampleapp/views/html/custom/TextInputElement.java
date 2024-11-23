package com.simonebasile.sampleapp.views.html.custom;


import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

/**
 * Represents a text input element in an HTML page.
 */
public class TextInputElement extends IHtmlElement {

    private final HtmlElement container;
    private final HtmlElement input;

    public TextInputElement(String name, String labelText) {
        String id = IdGenerator.get();
        container = div()
                .attr("class", "input-container")
                .content(
                        input = input().attr("type", "text", "name", name, "id", id),
                        label().text(labelText).attr("for", id)
                );
    }

    @Override
    public void write(OutputStream out) throws IOException {
        container.write(out);
    }

    public TextInputElement typePassword() {
        input.attr("type", "password");
        return this;
    }

    public TextInputElement value(String value) {
        if(value == null) value = "";
        input.attr("value", value);
        return this;
    }

    public TextInputElement style(String style) {
        container.attr("style", style);
        return this;
    }

}
