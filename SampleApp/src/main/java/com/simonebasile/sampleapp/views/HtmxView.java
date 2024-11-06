package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.Html5View;
import com.simonebasile.sampleapp.views.html.HtmlElement;

public class HtmxView extends Html5View {
    public HtmxView() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
        );
        addJs("https://unpkg.com/htmx.org@2.0.3");
    }
}
