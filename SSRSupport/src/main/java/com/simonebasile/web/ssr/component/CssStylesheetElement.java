package com.simonebasile.web.ssr.component;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a stylesheet element in an HTML page.
 */
public class CssStylesheetElement extends HtmlElement{
    public CssStylesheetElement(String script) {
        super("link", mkattr(script));
    }

    private static Map<String, String> mkattr(String script) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("rel", "stylesheet");
        attributes.put("href", script);
        return attributes;
    }
}
