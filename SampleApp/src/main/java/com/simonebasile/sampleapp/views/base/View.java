package com.simonebasile.sampleapp.views.base;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class View extends BaseView{
    public View() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
        );
        addCss("/static/common.css");
        addJs("/static/common.js");
    }

    public void pathChange(String path) {
        addHead(new HtmlElement("script")
                .attr("type", "javascript")
                .text("window.history.replaceState(null, null, " + path + ");"));
    }
}
