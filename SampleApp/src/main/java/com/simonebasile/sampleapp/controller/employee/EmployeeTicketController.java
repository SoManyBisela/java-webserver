package com.simonebasile.sampleapp.controller.employee;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.ResponseBody;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.dto.*;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.EmployeeTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Controller for the employee ticket detail section
 */
@Slf4j
public class EmployeeTicketController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public EmployeeTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Handles the GET request.
     * Renders the ticket detail section for the id in the request.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        Ticket ticket = ticketService.getById(id.getId(), user);
        if(ticket == null) {
            throw new ShowableException("Ticket not found");
        }
        return new HttpResponse<>(new EmployeeTicketDetailSection(ticket, user));
    }

    /**
     * Handles the PUT request.
     * Updates the ticket.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends ResponseBody> handlePut(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Ticket ticket;
        EmployeeUpdateTicket body = FormHttpMapper.map(r.getBody(), EmployeeUpdateTicket.class);
        try {
            ticket = ticketService.update(body, user);
        } catch (UpdateTicketException e) {
            throw new ShowableException(e);
        }
        return new HttpResponse<>(new EmployeeTicketDetailSection(ticket, user));

    }
}
