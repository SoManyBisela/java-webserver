package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.chat.ChatSection;
import com.simonebasile.web.ssr.component.HtmlElement;
import com.simonebasile.web.ssr.component.NoElement;
import com.simonebasile.web.ssr.view.HtmxView;

import static com.simonebasile.web.ssr.component.HtmlElement.div;
/**
 * Represents the main view of the page.
 * This view contains the header with the links to the other sections and the main content.
 */
public class MainView extends HtmxView {
    public MainView(User u) {
        addHead(
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png"),
                new HtmlElement("link").attr("rel", "preconnect", "href", "https://fonts.googleapis.com"),
                new HtmlElement("link").attr("rel", "preconnect", "href", "https://fonts.gstatic.com", "crossorigin", "true"),
                new HtmlElement("link").attr("rel", "stylesheet", "href", "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap")
        );
        addCss("/static/userview.css");
        addCss("https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@24,400,0,0");
        addJs("/static/extensions.js");
        addJs("https://unpkg.com/htmx.org@1.9.12/dist/ext/ws.js");
        addJs("https://unpkg.com/htmx-ext-debug@2.0.0/debug.js");
        addContent(

                div().attr("class", "header").content(
                        new NavbarButtons(u)
                ),
                div().attr("class", "container").content(
                        div().attr("class", "main", "id", "main")
                                .text("Seleziona un elemento sulla sinistra"),
                        (u.getRole() == Role.employee || u.getRole() == Role.user) ?
                                new ChatSection() : NoElement.instance
                )
        );
    }


}