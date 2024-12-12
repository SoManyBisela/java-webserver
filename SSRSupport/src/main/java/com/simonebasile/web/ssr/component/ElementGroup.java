package com.simonebasile.web.ssr.component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a group of components of an HTML page.
 */
public class ElementGroup implements IHtmlElement {
    protected final List<IHtmlElement> content;

    /**
     * Creates a new element group with no content.
     */
    public ElementGroup() {
        this.content = new ArrayList<>();
    }
    /**
     * Creates a new element group with the given content.
     *
     * @param content the content of the element group
     */
    public ElementGroup(List<? extends IHtmlElement> content) {
        this.content = new ArrayList<>(content);
    }
    /**
     * Creates a new element group with the given content.
     *
     * @param elements the content of the element group
     */
    public ElementGroup(IHtmlElement... elements) {
        this.content = Arrays.asList(elements);
    }

    /**
     * Writes the elements to the given output stream.
     *
     * @param os the output stream to write to
     */
    @Override
    public void write(OutputStream os) throws IOException {
        for (IHtmlElement htmlElement : content) {
            htmlElement.write(os);
        }
    }
}
