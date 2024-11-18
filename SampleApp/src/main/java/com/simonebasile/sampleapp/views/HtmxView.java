package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.Html5View;
import com.simonebasile.sampleapp.views.html.HtmlElement;

public class HtmxView extends Html5View {
    public HtmxView() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("meta").attr("name", "viewport", "content", "width=device-width, initial-scale=1")
        );
        //addJs("https://unpkg.com/htmx.org@2.0.3");
        addJs("/static/htmx.js");
    }
}
