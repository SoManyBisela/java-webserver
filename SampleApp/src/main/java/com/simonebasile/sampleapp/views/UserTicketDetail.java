package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.views.base.View;

import static com.simonebasile.sampleapp.views.html.HtmlElement.*;

public class UserTicketDetail extends View {
    public UserTicketDetail(Ticket ticket) {
        addContent(
                a().text("Back to tickets").attr("href", "/tickets")
        );
    }

    public UserTicketDetail(Ticket ticket, String message) {


    }
}
