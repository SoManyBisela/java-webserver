package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.chat.ChatSection;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class MainView extends HtmxView {
    public MainView(User u) {
        addHead(
                new HtmlElement("title").text("Ticketing"),
                new HtmlElement("link").attr("rel", "icon", "href", "/static/favicon.png")
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
                                div().attr("class", "chatbox closed").content(
                                        div().attr("class", "chatbox-content").content(
                                                div().attr("class", "chat-header").content(
                                                        chatboxToggle().text("X")
                                                ),
                                                new ChatSection()
                                        ),
                                        chatboxToggle().text("chat")
                                ) : NoElement.instance
                )
        );
    }

    private HtmlElement chatboxToggle() {
        return button().attr("class", "chatbox-toggle", "onclick", "htmx.closest(this, '.chatbox').classList.toggle('closed')");
    }
}