package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;
import static com.simonebasile.sampleapp.views.html.HtmlElement.td;

public class EmployeeTicketsView extends View {
    public EmployeeTicketsView(List<Ticket> tickets) {
        url("/tickets");
        addJs("/static/click-actions.js");
        addContent(
                h(1).text("Tickets")
        );
        if(!tickets.isEmpty()) {
            HtmlElement heading = tr().content(
                    th().text("Object"),
                    th().text("State"),
                    th().text("Assignee"),
                    th().text("Owner")
            );
            List<HtmlElement> rows = tickets.stream().map(EmployeeTicketsView::toRow).toList();
            addContent(
                    div().attr("class", "ticket-container")
                            .content(table().content(heading).content(rows)));
        } else {
            addContent(
                    div().attr("class", "empty-tickets")
                            .content(p().text("You have no tickets")));
        }
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .attr("form-action", "/ticket",
                        "form-method", "GET",
                        "form-param-id", ticket.getId()
                ).content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState().name()),
                        td().text(ticket.getAssegnee() == null ? "None" : ticket.getAssegnee()),
                        td().text(ticket.getOwner())
                );
    }
}
