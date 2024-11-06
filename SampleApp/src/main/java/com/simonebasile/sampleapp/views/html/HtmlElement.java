package com.simonebasile.sampleapp.views.html;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class HtmlElement extends ElementGroup{
    private final String name;
    private final Map<String, String> attributes;


    public HtmlElement(String name, List<? extends IHtmlElement> content) {
        super(content);
        this.name = name;
        this.attributes = new HashMap<>();
    }

    public HtmlElement(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public HtmlElement(String name) {
        this.name = name;
        this.attributes = new HashMap<>();
    }

    public HtmlElement(String name, Map<String, String> attributes, List<? extends IHtmlElement> content) {
        super(content);
        this.name = name;
        this.attributes = attributes;
    }


    public void write(OutputStream os) throws IOException {
        writeStr("<", os);
        writeStr(name, os);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            writeStr(" ", os);
            String key = attr.getKey();
            String value = attr.getValue();
            writeStr(key, os);
            writeStr("=", os);
            writeStr(escapeWrap(value), os);
        }
        writeStr(">", os);
        super.write(os);
        writeStr("</", os);
        writeStr(name, os);
        writeStr(">", os);
    }

    public static String escapeWrap(String value) {
        StringBuilder s = new StringBuilder();
        // TODO replace using this spec https://html.spec.whatwg.org/#character-references
        s.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '"') {
                s.append("&#34;");
            } else {
                s.append(c);
            }
        }
        s.append('"');
        return s.toString();

    }

    public static String strWrap(String value) {
        StringBuilder s = new StringBuilder();
        s.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if(c == '\\') {
                s.append("\\\\");
            } else if(c == '"') {
                s.append("\\\"");
            } else if(c == '\n') {
                s.append("\\n");
            }else if(c == '\t') {
                s.append("\\t");
            } else if(c == '\r'){
                s.append("\\r");
            } else {
                s.append(c);
            }
        }
        s.append('"');
        return s.toString();
    }

    private static void writeStr(String s, OutputStream os) throws IOException {
        os.write(s.getBytes(StandardCharsets.UTF_8));
    }

    public HtmlElement attr(String name, String value, String ...more) {
        if(more.length %2 != 0) {
            throw new IllegalArgumentException("additional attributes length should be even");
        }
        this.attributes.put(name, value);
        for(int i = 0; i < more.length; i += 2) {
            this.attributes.put(more[i], more[i + 1]);
        }
        return this;
    }

    public HtmlElement resetContent() {
        this.content.clear();
        return this;
    }

    //TODO distinguish between addX and X methods where addX adds to the existing data, and X replaces existing data

    public HtmlElement content(Collection<? extends IHtmlElement> elements) {
        content.addAll(elements);
        return this;
    }

    public HtmlElement content(IHtmlElement element, IHtmlElement ...more) {
        content.add(element);
        for(IHtmlElement child : more) {
            content.add(child);
        }
        return this;
    }

    public HtmlElement text(String text) {
        content.add(new TextElement(Objects.requireNonNull(text)));
        return this;
    }

    /* Htmx attributes */

    public HtmlElement hxGet(String url) {
        return attr("hx-get", url);
    }

    public HtmlElement hxPost(String url) {
        return attr("hx-post", url);
    }

    public HtmlElement hxPut(String url) {
        return attr("hx-put", url);
    }

    public HtmlElement hxDelete(String url) {
        return attr("hx-delete", url);
    }

    public HtmlElement hxTrigger(String value) {
        return attr("hx-trigger", value);
    }

    public HtmlElement hxTarget(String value) {
        return attr("hx-target", value);
    }

    public HtmlElement hxSync(String value) {
        return attr("hx-sync", value);
    }

    public HtmlElement hxSwap(String value) {
        return attr("hx-swap", value);
    }


    public HtmlElement hxConfirm(String value) {
        return attr("hx-confirm", value);
    }

    public HtmlElement hxVals(String... attrs) {
        if(attrs.length % 2 != 0) {
            throw new IllegalArgumentException("additional attributes length should be even");
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for(int i = 0; i < attrs.length; i += 2) {
            String name = attrs[i];
            String value = attrs[i + 1];
            builder.append(strWrap(name)).append(":").append(strWrap(value));
        }
        builder.append("}");

        return attr("hx-vals", builder.toString());
    }

    /* Common used elements */

    public static HtmlElement div() {
        return new HtmlElement("div");
    }

    public static HtmlElement h(int i) {
        return new HtmlElement("h" + i);
    }

    public static HtmlElement a() {
        return new HtmlElement("a");
    }

    public static HtmlElement p() {
        return new HtmlElement("p");
    }

    public static HtmlElement table() {
        return new HtmlElement("table");
    }

    public static HtmlElement tr() {
        return new HtmlElement("tr");
    }

    public static HtmlElement td() {
        return new HtmlElement("td");
    }

    public static HtmlElement th() {
        return new HtmlElement("th");
    }

    public static HtmlElement option() {
        return new HtmlElement("option");
    }

    public static HtmlElement label() {
        return new HtmlElement("label");
    }

    public static HtmlElement span() {
        return new HtmlElement("span");
    }

    public static HtmlElement input() {
        return new HtmlElement("input");
    }

    public static HtmlElement button() {
        return new HtmlElement("button");
    }

    public static HtmlElement form() {
        return new HtmlElement("form");
    }

    public static HtmlElement select() {
        return new HtmlElement("select");
    }

}
