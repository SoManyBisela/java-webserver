package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.custom.ErrorMessage;
import com.simonebasile.sampleapp.views.html.custom.InputForm;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class CreateTicketView extends View {
    public CreateTicketView() {
        this(null);
    }
    public CreateTicketView(String message) {
        addContent(
                h(1).text("Create ticket"),
                new InputForm()
                        .action("/ticket/create", "POST")
                        .input("object", "text", i -> i.input()
                                .attr("value", "Descrizione del problema"))
                        .input("message", "text")
                        .button(a -> a.text("Create ticket"))
                );
        if(message != null && !message.isBlank()) {
            addContent(new ErrorMessage(message));
        }
    }
}