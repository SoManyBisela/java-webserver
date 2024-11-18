package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class SelectInputElement extends IHtmlElement {

    private final HtmlElement container;
    private final HtmlElement select;

    public record SelectOption(String value, String text) {
        public SelectOption(String value) {
            this(value, value);
        }
    }

    public SelectInputElement(String name, String labelText, SelectOption... options) {
        String id = IdGenerator.get();
        container = div()
                .attr("class", "input-container")
                .content(
                        select = select()
                                .attr("id", id, "name", name)
                                .content(Arrays.stream(options)
                                        .map(a -> option().text(a.text).attr("value", a.value))
                                        .toList()),
                        label().text(labelText).attr("for", id)
                );
    }


    @Override
    public void write(OutputStream out) throws IOException {
        container.write(out);
    }
}
