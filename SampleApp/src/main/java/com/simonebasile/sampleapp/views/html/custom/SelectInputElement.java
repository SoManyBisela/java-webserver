package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static com.simonebasile.sampleapp.views.html.HtmlElement.option;
import static com.simonebasile.sampleapp.views.html.HtmlElement.select;

public class SelectInputElement extends IHtmlElement {

    private final HtmlElement element;

    public record SelectOption(String value, String text) {
        public SelectOption(String value) {
            this(value, value);
        }
    }

    public SelectInputElement(SelectOption... options) {
        element = select()
                .content(Arrays.stream(options)
                        .map(a -> option().text(a.text).attr("value", a.value))
                        .toList());
    }

    public SelectInputElement name(String name) {
        element.attr("name", name);
        return this;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        element.write(out);
    }
}
