package com.simonebasile.sampleapp.views.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A button used as a form submit button.
 */
public class FormButton extends HtmlElement {
    private final IHtmlElement container;

    public FormButton() {
        super("button");
        attr("class", "default-button", "type", "submit");
        container = div().attr("class", "form-button").content(
                super::write
        );
    }

    @Override
    public void write(OutputStream os) throws IOException {
        container.write(os);
    }
}
