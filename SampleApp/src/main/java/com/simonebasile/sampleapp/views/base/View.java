package com.simonebasile.sampleapp.views.base;

import com.simonebasile.sampleapp.views.html.HtmlElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

/**
 * Represents a view of the page.
 * It includes the common css and js files.
 */
public class View extends Html5View {
    public View() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
        );
        addCss("/static/userview.css");
        addCss("https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0");
    }
}
