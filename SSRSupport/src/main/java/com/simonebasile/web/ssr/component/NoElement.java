package com.simonebasile.web.ssr.component;

import java.io.OutputStream;

/**
 * Utility class representing an empty element. It is useful to allow inlining of elements that may not need to be added to the page.
 * This class is a singleton.
 */
public class NoElement implements IHtmlElement{
    public static final NoElement instance = new NoElement();
    private NoElement(){}
    @Override
    public void write(OutputStream out) {}
}
