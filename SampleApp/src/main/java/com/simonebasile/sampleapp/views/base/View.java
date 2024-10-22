package com.simonebasile.sampleapp.views.base;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class View extends BaseView{
    public View() {
        this(true);
    }
    public View(boolean withHeader) {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
        );
        addCss("/static/common.css");
        addJs("/static/common.js");
        //Page header
        if(withHeader) {
            addContent(
                    div().attr("class", "header")
                            .content(
                                    a().attr("href", "/").text("Go to homepage")
                            )
            );
        }
    }

    public void url(String path) {
        addHead(new HtmlElement("script")
                .attr("type", "javascript")
                .text("addonload(() => window.history.replaceState(null, null, " + HtmlElement.wrap(path) + "));"));
    }
}
