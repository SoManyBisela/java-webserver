package com.simonebasile.sampleapp.controller.employee;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.assertions.UnreachableBranchException;
import com.simonebasile.sampleapp.dto.*;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.EmployeeTicketDetailSection;
import com.simonebasile.sampleapp.views.TicketNotFoundSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class EmployeeTicketController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public EmployeeTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        Ticket ticket = ticketService.getById(id.getId(), user);
        if(ticket == null) {
            return new HttpResponse<>(r.getVersion(), 404, new TicketNotFoundSection(id.getId()));
        }
        return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user));
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Ticket ticket;
        EmployeeUpdateTicket body = FormHttpMapper.map(r.getBody(), EmployeeUpdateTicket.class);
        try {
            ticket = ticketService.update(body, user);
        } catch (UpdateTicketException e) {
            ticket = ticketService.getById(body.getId(), user);
            return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user, e.getMessage()));
        }
        return new HttpResponse<>(r.getVersion(), new EmployeeTicketDetailSection(ticket, user).successMessage("Ticket saved"));

    }
}
