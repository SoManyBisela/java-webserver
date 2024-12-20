package com.simonebasile.web.ssr.component;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a script element in an HTML page.
 */
public class JsScriptElement extends HtmlElement{
    public JsScriptElement(String script) {
        super("script", mkattr(script));
    }

    private static Map<String, String> mkattr(String script) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("src", script);
        return attributes;
    }
}
