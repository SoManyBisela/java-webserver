package com.simonebasile.sampleapp.views.html.custom;


import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class TextAreaElement extends IHtmlElement {

    private final HtmlElement container;
    private final HtmlElement input;

    public TextAreaElement(String name, String labelText) {
        String id = IdGenerator.get();
        container = div()
                .attr("class", "input-container")
                .content(
                        input = textarea().attr("type", "text",
                                "name", name,
                                "id", id,
                                "oninput", "this.style.height = ''; this.style.height = this.scrollHeight +'px'"
                        ),
                        label().text(labelText).attr("for", id)
                );
    }

    @Override
    public void write(OutputStream out) throws IOException {
        container.write(out);
    }

    public TextAreaElement value(String value) {
        if(value == null) value = "";
        input.text(value);
        return this;
    }

}
