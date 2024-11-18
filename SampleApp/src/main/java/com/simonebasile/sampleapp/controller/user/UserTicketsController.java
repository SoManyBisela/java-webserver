package com.simonebasile.sampleapp.controller.user;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.base.Html5View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import com.simonebasile.sampleapp.views.EmployeeTicketsSection;
import com.simonebasile.sampleapp.views.UserTicketsSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class UserTicketsController extends MethodHandler<InputStream, ApplicationRequestContext> {
    private final TicketService ticketService;


    public UserTicketsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        List<Ticket> tickets = ticketService.getByOwner(context.getLoggedUser().getUsername());
        return new HttpResponse<>(r.getVersion(), new UserTicketsSection(tickets));
    }
}
