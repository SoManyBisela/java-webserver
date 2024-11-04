package com.simonebasile.sampleapp.views.base;

import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.views.html.CssStylesheetElement;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.IHtmlElement;
import com.simonebasile.sampleapp.views.html.JsScriptElement;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BaseView implements HttpResponse.ResponseBody {
    private List<IHtmlElement> headElements;
    private List<String> scripts;
    private List<String> stylesheets;
    private List<IHtmlElement> content;

    public BaseView() {
        this.headElements = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.stylesheets = new ArrayList<>();
        this.content = new ArrayList<>();
    }

    public BaseView(List<? extends IHtmlElement> content) {
        this.headElements = new ArrayList<>();
        this.scripts = new ArrayList<>();
        this.stylesheets = new ArrayList<>();
        this.content = new ArrayList<>(content);
    }

    public BaseView addHead(IHtmlElement el, IHtmlElement... more) {
        headElements.add(el);
        for(IHtmlElement e : more) {
            headElements.add(e);
        }
        return this;
    }

    public BaseView addJs(String script, String... more) {
        scripts.add(script);
        for(String m: more) {
            scripts.add(m);
        }
        return this;
    }

    public BaseView addCss(String script, String ...more) {
        stylesheets.add(script);
        for(String m: more) {
            stylesheets.add(m);
        }
        return this;
    }

    public BaseView addContent(IHtmlElement el, IHtmlElement... more) {
        content.add(el);
        for (IHtmlElement htmlElement : more) {
            content.add(htmlElement);
        }
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

    @Override
    public Long contentLength() {
        return null;
    }

    @Override
    public String contentType() {
        return "text/html";
    }
}
