package com.simonebasile.sampleapp.views.base;

import com.simonebasile.sampleapp.views.html.CssStylesheetElement;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.JsScriptElement;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Html5View extends IHtmlElement {
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
