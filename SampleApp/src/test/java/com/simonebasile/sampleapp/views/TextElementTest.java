package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.TextElement;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TextElementTest {

    @Test
    public void testEscaping() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        new TextElement("<script>alert('ciao')</script>").write(os);
        String string = os.toString();
        assertFalse(string.contains("<"));
        assertFalse(string.contains(">"));
    }

}
