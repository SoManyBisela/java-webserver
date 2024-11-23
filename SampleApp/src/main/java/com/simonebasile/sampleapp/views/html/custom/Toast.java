package com.simonebasile.sampleapp.views.html.custom;

import com.simonebasile.sampleapp.views.html.HtmlElement;

/**
 * Represents a toast message in an HTML page.
 */
public class Toast extends HtmlElement {
    public Toast(String message, String addClass) {
        super("div");
        attr(
                "class", "stack-horizontal toast " + addClass,
                "hx-sle-onload", "this.style.opacity = 0;this.style.transition = 'opacity 3s ease-in';setTimeout(() => this.remove(), 3000)",
                "onclick", "this.remove()"
        );
        hxExt("simple-loaded-event");
        text(message);
    }
}
