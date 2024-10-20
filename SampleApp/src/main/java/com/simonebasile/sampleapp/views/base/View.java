package com.simonebasile.sampleapp.views.base;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class View extends BaseView{
    public View() {
        addHead(
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
        );
        addCss("/static/common.css");
        addJsScript("/static/common.js");
    }
}
