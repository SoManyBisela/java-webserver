package com.simonebasile.sampleapp.views.html;

import java.io.OutputStream;

public class NoElement extends IHtmlElement{
    public static final NoElement instance = new NoElement();
    private NoElement(){}
    @Override
    public void write(OutputStream out) {}
}
