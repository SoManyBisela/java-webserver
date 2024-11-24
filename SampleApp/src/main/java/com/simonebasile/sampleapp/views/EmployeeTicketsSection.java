package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

/**
 * Represents the list of tickets accessible to an employee.
 */
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
                    th().text("Owner"),
                    th()
            );
            List<HtmlElement> rows = tickets.stream().map(EmployeeTicketsSection::toRow).toList();
            content.add(
                    div().attr("class", "ticket-container")
                            .content(table().attr("class", "tickets-table").content(
                                            colgroup().content(
                                                    col(),
                                                    col().attr("style", "width: 1rem"),
                                                    col().attr("style", "width: 1rem"),
                                                    col().attr("style", "width: 1rem"),
                                                    col().attr("style", "width: 40px")
                                            ),
                                            heading
                                    ).content(rows)
                            )
            );
        } else {
            content.add(
                    div().attr("class", "empty-tickets")
                            .content(p().text("You have no tickets")));
        }
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState().name()),
                        td().text(ticket.getAssignee() == null ? "None" : ticket.getAssignee()),
                        td().text(ticket.getOwner()),
                        td().attr("class", "buttons-cell").content(
                                div().content(
                                        button().content(new MaterialIcon("visibility"))
                                                .attr("class", "button-icon")
                                                .hxGet("/ticket")
                                                .hxVals("id", ticket.getId())
                                                .hxSwap("inner-html")
                                                .hxTarget("#main")
                                )
                        )
                );
    }
}
