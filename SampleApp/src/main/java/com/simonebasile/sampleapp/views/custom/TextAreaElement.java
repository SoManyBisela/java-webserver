package com.simonebasile.sampleapp.views.custom;


import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.component.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;

import static com.simonebasile.web.ssr.component.HtmlElement.*;
/**
 * Represents a text area element in an HTML page.
 */
public class TextAreaElement implements IHtmlElement {

    private final HtmlElement container;
    private final HtmlElement input;

    public TextAreaElement(String name, String labelText) {
        String id = IdGenerator.get();
        String autosize = "this.style.height = ''; this.style.height = this.scrollHeight +'px'";
        container = div()
                .attr("class", "input-container")
                .content(
                        input = textarea().hxExt("simple-loaded-event")
                                .attr("type", "text",
                                        "name", name,
                                        "id", id,
                                        "hx-sle-onload", autosize + ";setTimeout(() => this.classList.remove('animate'), 150)",
                                        "oninput", autosize,
                                        "class", "animate",
                                        "autocomplete", "off"
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

    public TextAreaElement readonly() {
        input.attr("readonly", "true");
        return this;
    }

}
