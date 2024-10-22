package com.simonebasile.sampleapp.views.html;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


interface IHtmlElement {
    void write(OutputStream os) throws IOException;
}

public class HtmlElement implements IHtmlElement {
    private final String name;
    private final Map<String, String> attributes;
    private final List<IHtmlElement> content;

    private record TextElement(String text) implements IHtmlElement {
        @Override
        public void write(OutputStream os) throws IOException {
            writeStr(text, os);
        }
    }

    public HtmlElement(String name, List<? extends HtmlElement> content) {
        this.name = name;
        this.content = new ArrayList<>(content);
        this.attributes = new HashMap<>();
    }

    public HtmlElement(String name, Map<String, String> attributes) {
        this.name = name;
        this.content = new ArrayList<>();
        this.attributes = attributes;
    }

    public HtmlElement(String name) {
        this.name = name;
        this.content = new ArrayList<>();
        this.attributes = new HashMap<>();
    }

    public HtmlElement(String name, Map<String, String> attributes, List<? extends HtmlElement> content) {
        this.name = name;
        this.attributes = attributes;
        this.content = new ArrayList<>(content);
    }


    public void write(OutputStream os) throws IOException {
        writeStr("<", os);
        writeStr(name, os);
        writeStr(" ", os);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            String key = attr.getKey();
            String value = attr.getValue();
            writeStr(key, os);
            writeStr("=", os);
            writeStr(wrap(value), os);
        }
        writeStr(">", os);
        for (IHtmlElement htmlElement : content) {
            htmlElement.write(os);
        }
        writeStr("</", os);
        writeStr(name, os);
        writeStr(">", os);
    }

    public static String wrap(String value) {
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

    //TODO distinguish between addX and X methods where addX adds to the existing data, and X replaces existing data

    public HtmlElement content(Collection<? extends HtmlElement> elements) {
        content.addAll(elements);
        return this;
    }

    public HtmlElement content(HtmlElement element, HtmlElement ...more) {
        content.add(element);
        for(HtmlElement child : more) {
            content.add(child);
        }
        return this;
    }

    public HtmlElement text(String text) {
        content.add(new TextElement(text));
        return this;
    }

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

}
