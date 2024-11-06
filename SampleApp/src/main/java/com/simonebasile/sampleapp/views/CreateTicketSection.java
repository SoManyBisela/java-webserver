package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.InputForm;

import static com.simonebasile.sampleapp.views.html.HtmlElement.h;

public class CreateTicketSection extends ElementGroup {
    public CreateTicketSection() {
        this(null);
    }
    public CreateTicketSection(String message) {
        content.add(h(1).text("Create ticket"));
        content.add(new InputForm()
                        .hxPost("/ticket/create")
                        .hxTarget("#main")
                        .input("object", "text", i -> i.input()
                                .attr("placeholder", "Descrizione del problema"))
                        .input("message", "text")
                        .button(a -> a.text("Create ticket").attr("type", "submit"))
                );
        if(message != null && !message.isBlank()) {
            content.add(new ErrorMessage(message));
        }
    }
}
