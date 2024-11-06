package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.html.ElementGroup;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class TicketNotFoundSection extends ElementGroup {

    public TicketNotFoundSection(String id) {
        content.add(h(1).text("Not found"));
        content.add(p().text("Ticket with id " + id + " does not exist"));
    }
}
