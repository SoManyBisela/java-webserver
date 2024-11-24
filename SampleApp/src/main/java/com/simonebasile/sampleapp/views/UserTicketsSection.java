package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.html.custom.FormButton;
import com.simonebasile.sampleapp.views.html.custom.MaterialIcon;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;
/**
 * Represents the list of tickets accessible to a user.
 */
public class UserTicketsSection extends ElementGroup {
    public UserTicketsSection(List<Ticket> tickets) {
        super();
        HtmlElement stack;
        content.add(stack = div().attr("class", "stack-vertical", "style", "gap: 16px"));
        stack.content(
                h(1).text("Your tickets"),
                div().content(createTicketButton())
        );
        if (!tickets.isEmpty()) {
            HtmlElement heading = tr().content(
                    th().text("Object"),
                    th().text("State"),
                    th().text("Assigned"),
                    th()
            );
            List<HtmlElement> rows = tickets.stream().map(UserTicketsSection::toRow).toList();
            stack.content(
                    div().attr("class", "ticket-container")
                            .content(table().attr("class", "tickets-table").content(
                                            colgroup().content(
                                                    col(),
                                                    col().attr("style", "width: 1rem"),
                                                    col().attr("style", "width: 1rem"),
                                                    col().attr("style", "width: 80px")
                                            ),
                                            heading
                                    ).content(rows)
                            )
            );
        } else {
            stack.content(
                    div().attr("class", "empty-tickets")
                            .content(p().text("You have no tickets")));
        }
    }


    private HtmlElement createTicketButton() {
        return button()
                .attr("class", "default-button")
                .hxGet("/ticket")
                .hxSwap("inner-html")
                .hxTarget("#main")
                .content(
                        new MaterialIcon("add"),
                        span().text("Create ticket")
                );

    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr()
                .content(
                        td().text(ticket.getObject()),
                        td().text(ticket.getState().name()),
                        td().text(ticket.getAssignee() != null ? "Yes" : "No"),
                        td().attr("class", "buttons-cell").content(
                                div().content(
                                        button().content(new MaterialIcon("visibility"))
                                                .attr("class", "button-icon")
                                                .hxGet("/ticket")
                                                .hxVals("id", ticket.getId())
                                                .hxSwap("inner-html")
                                                .hxTarget("#main"),
                                        button().content(new MaterialIcon("delete"))
                                                .attr("class", "delete-button")
                                                .hxDelete("/ticket")
                                                .hxVals("id", ticket.getId())
                                                .hxSwap("delete")
                                                .hxConfirm("Are you sure you want to delete this ticket?")
                                                .hxTarget("closest tr")
                                )
                        )
                );
    }


}
