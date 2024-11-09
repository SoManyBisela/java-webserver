package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.chat.ChatSection;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.NoElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class MainView extends HtmxView {
    public MainView(User u) {
        addCss("/static/userview.css");
        addJs("/static/body-file-extension.js");
        addJs("https://unpkg.com/htmx.org@1.9.12/dist/ext/ws.js");
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
        //TODO add icon instead of M
        return button().attr("class", "chatbox-toggle", "onclick", "htmx.closest(this, '.chatbox').classList.toggle('closed')");
    }
}