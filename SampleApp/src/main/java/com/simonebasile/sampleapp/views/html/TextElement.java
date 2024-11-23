package com.simonebasile.sampleapp.views.html;

import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Getter
/**
 * Represents the text content of an HTML tag.
 * it escapes the characters <, > and & to their respective HTML entities.
 */
public class TextElement extends IHtmlElement {

    private final static byte [] AMP = "&amp;".getBytes(StandardCharsets.UTF_8);
    private final static byte [] GT = "&gt;".getBytes(StandardCharsets.UTF_8);
    private final static byte [] LT = "&lt;".getBytes(StandardCharsets.UTF_8);

    private final String text;

    public TextElement(String text) {
        this.text = text;
    }

    /**
     * Writes the text to the given output stream.
     *
     * @param os the output stream to write to
     */
    @Override
    public void write(OutputStream os) throws IOException {
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        int rstart = 0;
        int rend = 0;
        while(rend < bytes.length) {
            byte b = bytes[rend];
            byte[] rpl = switch(b) {
                case '&' -> AMP;
                case '>' -> GT;
                case '<' -> LT;
                default -> null;
            };
            if(rpl != null) {
                os.write(bytes, rstart, rend - rstart);
                os.write(rpl);
                rstart = rend + 1;
            }
            rend++;
        }
        os.write(bytes, rstart, rend - rstart);
    }
}
