package com.simonebasile.sampleapp.controller.employee;

import com.simonebasile.http.HttpHeaders;
import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.views.EmployeeTicketsSection;
import com.simonebasile.sampleapp.views.UserTicketsSection;
import com.simonebasile.sampleapp.views.base.Html5View;
import com.simonebasile.sampleapp.views.html.HtmlElement;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class EmployeeTicketsController extends MethodHandler<InputStream, ApplicationRequestContext> {
    private final TicketService ticketService;

    public EmployeeTicketsController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        List<Ticket> tickets = ticketService.getSubmitted();
        return new HttpResponse<>(r.getVersion(), new EmployeeTicketsSection(tickets));
    }
}
