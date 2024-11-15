package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketsSection extends ElementGroup {
    public UserTicketsSection(List<Ticket> tickets) {
        this(tickets, null);
    }
    public UserTicketsSection(List<Ticket> tickets, String errorMessage) {
        super();
        content.add(
                h(1).text("Your tickets")
        );
        if(!tickets.isEmpty()) {
            HtmlElement heading = tr().content(
                    th().text("Object"),
                    th().text("State"),
                    th().text("Assigned")
            );
            List<HtmlElement> rows = tickets.stream().map(UserTicketsSection::toRow).toList();
            content.add(
                    div().attr("class", "ticket-container")
                            .content(table().content(heading).content(rows)));
        } else {
            content.add(
                    div().attr("class", "empty-tickets")
                            .content(p().text("You have no tickets")));
        }
        content.add(createTicketButton());
    }


    private HtmlElement createTicketButton() {
        return button()
                .hxGet("/ticket/create")
                .hxSwap("inner-html")
                .hxTarget("#main")
                .text("Create ticket");
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .hxGet("/ticket")
                .hxVals("id", ticket.getId())
                .hxSwap("inner-html")
                .hxTarget("#main")
                .hxSync("next button:abort")
                .content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState().name()),
                        td().text(ticket.getAssignee() != null ? "Yes" : "No"),
                        td().content(button().text("Delete")
                                .hxDelete("/ticket")
                                .hxVals("id", ticket.getId())
                                .hxSwap("delete")
                                .hxConfirm("Are you sure you want to delete this ticket?")
                                .hxTarget("closest tr"))
                );
    }
}
