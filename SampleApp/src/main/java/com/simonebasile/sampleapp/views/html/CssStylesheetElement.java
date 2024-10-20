package com.simonebasile.sampleapp.views.html;

import java.util.HashMap;
import java.util.Map;

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
