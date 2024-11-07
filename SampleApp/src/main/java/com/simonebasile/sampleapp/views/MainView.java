package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.IHtmlElement;


import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class MainView extends HtmxView {
    //This will be the main view that contains all the elements the user will interact with
    public MainView() {
        addCss("/static/userview.css");
        addJs("/static/custom.js");
        addContent(
                div().attr("class", "header"),
                div().attr("class", "container").content(
                        div().attr("class", "sidebar").content(
                                sidebarToggle(),
                                sidebarElements()),
                        div().attr("class", "main", "id", "main")
                                .text("Seleziona un elemento sulla sinistra")
                )
        );
    }

    private IHtmlElement sidebarToggle() {
        //TODO add icon instead of M
        return button().attr("onclick", "this.parentElement.classList.toggle('closed')").text("M");
    }

    private IHtmlElement sidebarElements() {
        return div().attr(
                "hx-trigger", "load",
                "hx-get", "/page-links",
                "hx-swap", "inner-html"
        ).text("Loading");
    }
}
