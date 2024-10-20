package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.views.base.View;
import com.simonebasile.sampleapp.views.html.HtmlElement;

import java.util.List;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserPageView extends View {
    public UserPageView(List<Ticket> tickets) {
        List<HtmlElement> rows = tickets.stream().map(UserPageView::toRow).toList();
        HtmlElement heading = tr().content(
                th().text("Oggetto"),
                th().text("Stato"),
                th().text("Preso in carico"),
                th().text("actions")
        );
        addContent(
                h(1).text("User page"),
                div().attr("class", "ticket-container")
                                .content(table().content(heading).content(rows))
        );
    }

    private static HtmlElement toRow(Ticket ticket) {
        return tr().content(
                td().text(ticket.getObject()),
                td().text(ticket.getState()),
                td().text(ticket.getEmployee() != null ? "Yes" : "No"),
                td().content(modifyButton(ticket.getId()))
        );
    }

    private static HtmlElement modifyButton(String id) {
        //TODO change action
        return form().attr("action", "edit", "method", "POST")
                .content(button().attr("type", "submit").text("Edit"));
    }
}
