package com.simonebasile.sampleapp.views.htmx;

import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.IHtmlElement;

import java.util.ArrayList;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserView extends HtmxView {
    //This will be the main view that contains all the elements the user will interact with
    public UserView() {
        addCss("/static/userview.css");
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
        return button().attr("onclick", "this.parentElement.classList.toggle('closed')").text("T");
    }

    private IHtmlElement sidebarElements() {
        return div().attr(
                "hx-trigger", "load",
                "hx-get", "/page-links",
                "hx-swap", "inner-html"
        ).text("Loading");
    }
}
