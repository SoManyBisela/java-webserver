package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class EmployeeTicketsSection extends ElementGroup {
    public EmployeeTicketsSection(List<Ticket> tickets) {
        content.add(
                h(1).text("Tickets")
        );
        if(!tickets.isEmpty()) {
            HtmlElement heading = tr().content(
                    th().text("Object"),
                    th().text("State"),
                    th().text("Assignee"),
                    th().text("Owner")
            );
            List<HtmlElement> rows = tickets.stream().map(EmployeeTicketsSection::toRow).toList();
            content.add(
                    div().attr("class", "ticket-container")
                            .content(table().content(heading).content(rows)));
        } else {
            content.add(
                    div().attr("class", "empty-tickets")
                            .content(p().text("You have no tickets")));
        }
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .hxGet("/ticket")
                .hxVals("id", ticket.getId())
                .hxTarget("#main")
                .content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState().name()),
                        td().text(ticket.getAssegnee() == null ? "None" : ticket.getAssegnee()),
                        td().text(ticket.getOwner())
                );
    }
}
