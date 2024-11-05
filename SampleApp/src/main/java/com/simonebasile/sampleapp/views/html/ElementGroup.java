package com.simonebasile.sampleapp.views.html;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementGroup extends IHtmlElement {
    protected final List<IHtmlElement> content;

    protected ElementGroup() {
        this.content = new ArrayList<>();
    }
    public ElementGroup(List<? extends IHtmlElement> content) {
        this.content = new ArrayList<>(content);
    }
    public ElementGroup(IHtmlElement... elements) {
        this.content = Arrays.asList(elements);
    }

    @Override
    public void write(OutputStream os) throws IOException {
        for (IHtmlElement htmlElement : content) {
            htmlElement.write(os);
        }
    }
}
