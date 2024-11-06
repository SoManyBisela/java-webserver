package com.simonebasile.sampleapp.views.html;

import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Getter
public class TextElement extends IHtmlElement {
    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        os.write(text.getBytes(StandardCharsets.UTF_8));
    }
}
