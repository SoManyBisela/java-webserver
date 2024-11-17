package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.TextInputElement;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class CreateTicketSection extends ElementGroup {
    public CreateTicketSection() {
        this(null);
    }

    public CreateTicketSection(String message) {
        content.add(h(1).text("Create ticket"));
        content.add(form()
                .hxPost("/ticket/create")
                .hxTarget("#main")
                .content(
                        div().attr("class", "stack-vertical")
                                .content(
                                        new TextInputElement("object", "Object"),
                                        new TextInputElement("message", "Description"),
                                        button().text("Create ticket").attr("type", "submit")
                                )
                )
        );
        if (message != null && !message.isBlank()) {
            content.add(new ErrorMessage(message));
        }
    }
}
