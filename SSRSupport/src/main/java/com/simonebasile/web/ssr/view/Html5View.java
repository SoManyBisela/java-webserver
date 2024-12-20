package com.simonebasile.web.ssr.view;

import com.simonebasile.web.ssr.component.CssStylesheetElement;
import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.component.IHtmlElement;
import com.simonebasile.web.ssr.component.JsScriptElement;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a generic HTML5 view.
 */
public class Html5View implements IHtmlElement {
    private List<IHtmlElement> headElements;
    private List<String> scripts;
    private List<String> stylesheets;
    private List<IHtmlElement> content;

    public Html5View() {
        this.headElements = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.stylesheets = new ArrayList<>();
        this.content = new ArrayList<>();
    }

    public Html5View(List<? extends IHtmlElement> content) {
        this.headElements = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.stylesheets = new ArrayList<>();
        this.content = new ArrayList<>(content);
    }

    public Html5View addHead(IHtmlElement el, IHtmlElement... more) {
        headElements.add(el);
        headElements.addAll(Arrays.asList(more));
        return this;
    }

    public Html5View addJs(String script, String... more) {
        scripts.add(script);
        scripts.addAll(Arrays.asList(more));
        return this;
    }

    public Html5View addCss(String script, String ...more) {
        stylesheets.add(script);
        stylesheets.addAll(Arrays.asList(more));
        return this;
    }

    public Html5View addContent(IHtmlElement el, IHtmlElement... more) {
        content.add(el);
        content.addAll(Arrays.asList(more));
        return this;
    }

    /**
     * Writes the HTML5 view to the output stream.
     * @param out the output stream to write to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(OutputStream out) throws IOException {
        List<IHtmlElement> elements = new ArrayList<>();
        Stream.of(
                        headElements.stream(),
                        scripts.stream().map(JsScriptElement::new),
                        stylesheets.stream().map(CssStylesheetElement::new)
                )
                .flatMap(a -> a)
                .forEach(elements::add);
        HtmlElement head = new HtmlElement("head", elements );
        HtmlElement body = new HtmlElement("body", content );
        out.write("<!DOCTYPE html>".getBytes(StandardCharsets.UTF_8));
        new HtmlElement("html", List.of(head, body)).write(out);
    }

}
