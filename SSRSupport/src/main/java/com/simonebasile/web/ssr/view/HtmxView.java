package com.simonebasile.web.ssr.view;

import com.simonebasile.web.ssr.component.HtmlElement;

/**
 * Represents a view that uses htmx.
 */
public class HtmxView extends Html5View {
    public HtmxView() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("meta").attr("name", "viewport", "content", "width=device-width, initial-scale=1")
        );
        addJs("https://unpkg.com/htmx.org@2.0.3");
    }
}
