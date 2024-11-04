package com.simonebasile.sampleapp.views.html;

import java.io.IOException;
import java.io.OutputStream;

public interface IHtmlElement {
    void write(OutputStream os) throws IOException;
}
