package com.simonebasile.sampleapp.views.html;

import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Getter
public class TextElement implements IHtmlElement {
    private final String text;

    TextElement(String text) {
        this.text = text;
    }

    @Override
    public void write(OutputStream os) throws IOException {
        os.write(text.getBytes(StandardCharsets.UTF_8));
    }
}
