package com.simonebasile.sampleapp.views;

import com.simonebasile.sampleapp.views.base.View;

public class TicketNotFoundView extends View {
    public TicketNotFoundView(String id) {
        url("/ticket?id=" + id);
    }
}
