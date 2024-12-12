package com.simonebasile.sampleapp.views;


import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.view.Html5View;
/**
 * Represents a view of the page.
 * It includes the common css and js files.
 */
public class View extends Html5View {
    public View() {
        addHead(
                new HtmlElement("meta").attr("charset", "UTF-8"),
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png"),
                new HtmlElement("link").attr("rel", "preconnect", "href", "https://fonts.googleapis.com"),
                new HtmlElement("link").attr("rel", "preconnect", "href", "https://fonts.gstatic.com", "crossorigin", "true"),
                new HtmlElement("link").attr("rel", "stylesheet", "href", "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap")
        );
        addCss("/static/userview.css");
        addCss("https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0");
    }
}
