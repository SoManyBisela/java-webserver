package com.simonebasile.sampleapp.views.custom;


import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * Represents a toast message in an HTML page.
 */
public class Toast extends HtmlElement {
    public Toast(String message, String addClass) {
        super("div");
        attr("class", "toast-container");
        content(div()
                .hxExt("simple-loaded-event")
                .attr("class", "stack-horizontal toast " + addClass,
                "onclick", "this.parentElement.remove()",
                "hx-sle-onload", """
                        setTimeout(() => {
                            this.style.opacity = 0;
                            this.style.transition = 'opacity 3s ease-in';
                            setTimeout(() => this.parentElement.remove(), 3000)
                        }, 2000)""").content(span().text(message), new MaterialIcon("close", "toast-close")));
        ;
    }
}
