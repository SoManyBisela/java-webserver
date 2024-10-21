package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.ArrayList;
import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketsView extends View {
    private record Both(HtmlElement e1, HtmlElement e2){}
    public UserTicketsView(List<Ticket> tickets) {
        addContent(
                h(1).text("Your tickets")
        );
        if(!tickets.isEmpty()) {
            HtmlElement heading = tr().content(
                    th().text("Object"),
                    th().text("State"),
                    th().text("Assigned"),
                    th().text("actions")
            );
            List<HtmlElement> rows = tickets.stream().map(UserTicketsView::toRow).toList();
            addContent(
                    div().attr("class", "ticket-container")
                            .content(table().content(heading).content(rows)));
        } else {
            addContent(
                    div().attr("class", "empty-tickets")
                    .content(p().text("You have no tickets")));
        }
        addContent(createTicketButton());
    }

    private HtmlElement createTicketButton() {
        return form().attr("action", "/ticket/create", "method", "GET", "onsubmit", "test")
                .content(button().attr("type", "submit").text("Create Ticket"));
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .attr("form-action", "/ticket/detail",
                        "form-method", "GET",
                        "form-param-id", ticket.getId()
                ).content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState()),
                        td().text(ticket.getEmployee() != null ? "Yes" : "No")
                );
    }
}
