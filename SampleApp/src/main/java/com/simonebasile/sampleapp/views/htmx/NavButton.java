package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.views.html.HtmlElement;

public class NavButton extends HtmlElement {

    public NavButton(String text, String url, String target) {
        this(text, url, target, "/static/icon/placeholder.png");
    }

    public NavButton(String text, String url, String target, String icon) {
        super("div");
        content(
                div().attr("class", "nav-btn-ico", "style", "background-image: url(" + icon + ")"),
                span().attr("class", "nav-btn-text").text(text)
        );
        attr("class", "nav-btn btn");
        attr("hx-get", url);
        attr("hx-swap", "inner-html");
        attr("hx-target", target);
    }
}
