package com.simonebasile.web.ssr.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * class representing an HTML element
 */
public class HtmlElement extends ElementGroup{
    private final String name;
    private final Map<String, String> attributes;

    /**
     * Creates a new HTML element with the given name and content
     *
     * @param name the name of the element
     * @param content the content of the element
     */
    public HtmlElement(String name, List<? extends IHtmlElement> content) {
        super(content);
        this.name = name;
        this.attributes = new HashMap<>();
    }

    /**
     * Creates a new HTML element with the given name and attributes
     *
     * @param name the name of the element
     * @param attributes the attributes of the element
     */
    public HtmlElement(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    /**
     * Creates a new HTML element with the given name
     *
     * @param name the name of the element
     */
    public HtmlElement(String name) {
        this.name = name;
        this.attributes = new HashMap<>();
    }

    /**
     * Creates a new HTML element with the given name, attributes and content
     *
     * @param name the name of the element
     * @param attributes the attributes of the element
     * @param content the content of the element
     */
    public HtmlElement(String name, Map<String, String> attributes, List<? extends IHtmlElement> content) {
        super(content);
        this.name = name;
        this.attributes = attributes;
    }

    /**
     * Writes the HTML element to the output stream
     *
     * @param os the output stream to write the element in
     * @throws IOException in case an IOException occurred while writing the element
     */
    @Override
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

    /**
     * Escapes a string to be used as an attribute value
     *
     * @param value the value to escape
     * @return the escaped value
     */
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

    /**
     * Wraps a string in double quotes
     *
     * @param value the value to wrap
     * @return the wrapped value
     */
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

    /**
     * Utility method to write a string to an output stream
     * @param s the string to write
     * @param os the output stream to write the string in
     * @throws IOException in case an IOException occurred while writing the string
     */
    private static void writeStr(String s, OutputStream os) throws IOException {
        os.write(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Add attributes to the element
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @param more an array of even number of strings representing additional attributes
     * @return the element
     */
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

    /**
     * Removes all content from the element
     *
     * @return the element
     */
    public HtmlElement resetContent() {
        this.content.clear();
        return this;
    }

    /**
     * Adds content to the element
     *
     * @param elements the elements to add
     * @return the element
     */
    public HtmlElement content(Collection<? extends IHtmlElement> elements) {
        content.addAll(elements);
        return this;
    }

    /**
     * Adds content to the element
     *
     * @param element the element to add
     * @param more an array of elements to add
     * @return the element
     */
    public HtmlElement content(IHtmlElement element, IHtmlElement ...more) {
        content.add(element);
        content.addAll(Arrays.asList(more));
        return this;
    }

    /**
     * Adds text content to the element
     *
     * @param text the text to add
     * @return the element
     */
    public HtmlElement text(String text) {
        content.add(new TextElement(Objects.requireNonNull(text)));
        return this;
    }

    /* Shortcuts for htmx attributes */

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

    public HtmlElement hxSwapOob(String value) {
        return attr("hx-swap-oob", value);
    }

    public HtmlElement hxConfirm(String value) {
        return attr("hx-confirm", value);
    }

    public HtmlElement hxWsSend() {
        return attr("ws-send", "true", "hx-on::wsConfigSend", "evt.detail.headers = false");
    }

    /**
     * Creates the hx-vals attribute that must cotain a JSON object.
     * The JSON object is created by passing an even number of strings the same way attr works
     *
     * @param attrs the attributes to add to the JSON object
     */
    public HtmlElement hxVals(String... attrs) {
        if(attrs.length % 2 != 0) {
            throw new IllegalArgumentException("additional attributes length should be even");
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for(int i = 0; i < attrs.length; i += 2) {
            if(i != 0) builder.append(',');
            String name = attrs[i];
            String value = attrs[i + 1];
            builder.append(strWrap(name)).append(":").append(strWrap(value));
        }
        builder.append("}");

        return attr("hx-vals", builder.toString());
    }

    public HtmlElement hxExt(String s) {
        return attr("hx-ext", s);
    }

    /* Shortcuts for commonly used elements */

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

    public static HtmlElement thead() {
        return new HtmlElement("thead");
    }

    public static HtmlElement tbody() {
        return new HtmlElement("tbody");
    }

    public static HtmlElement colgroup() {
        return new HtmlElement("colgroup");
    }

    public static HtmlElement col() {
        return new HtmlElement("col");
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

    public static HtmlElement textarea() {
        return new HtmlElement("textarea");
    }

    public static HtmlElement button() {
        return new HtmlElement("button");
    }

    public static HtmlElement form() {
        return new HtmlElement("form").attr("autocomplete", "off");
    }

    public static HtmlElement select() {
        return new HtmlElement("select");
    }

}
