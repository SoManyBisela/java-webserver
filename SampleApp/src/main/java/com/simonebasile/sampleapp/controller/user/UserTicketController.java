package com.simonebasile.sampleapp.controller.user;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.http.response.HttpResponseBody;
import com.simonebasile.sampleapp.Utils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.interceptors.ShowableException;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import com.simonebasile.sampleapp.views.html.ElementGroup;
import com.simonebasile.sampleapp.views.html.custom.Toast;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Controller for the user ticket detail section
 */
@Slf4j
public class UserTicketController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public UserTicketController(TicketService ticketService) {
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
    protected HttpResponse<? extends HttpResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        Ticket ticket = ticketService.getById(id.getId(), user);
        return new HttpResponse<>(new UserTicketDetailSection(ticket));
    }

    /**
     * Handles the POST request.
     * Creates a new ticket.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        CreateTicket body = FormHttpMapper.map(r.getBody(), CreateTicket.class);
        Ticket ticket = new Ticket(body);
        String id;
        try {
            id = ticketService.createTicket(ticket, user).getId();
        } catch (CreateTicketException e) {
            throw new ShowableException(e);
        }
        Ticket t = ticketService.getById(id, user);
        return new HttpResponse<>(new ElementGroup(
                new UserTicketDetailSection(t),
                Utils.oobAdd("main", new Toast("Ticket saved", "success"))
        ));
    }

    /**
     * Handles the PUT request.
     * Updates the ticket.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handlePut(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Ticket ticket;
        UserUpdateTicket body = FormHttpMapper.map(r.getBody(), UserUpdateTicket.class);
        try {
            ticket = ticketService.update(body, user);
        } catch (UpdateTicketException e) {
            throw new ShowableException(e);
        }
        return new HttpResponse<>(new UserTicketDetailSection(ticket));
    }

    /**
     * Handles the DELETE request.
     * Deletes the ticket.
     * @param r the request
     * @param context the context
     * @return the response
     */
    @Override
    protected HttpResponse<? extends HttpResponseBody> handleDelete(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        if(ticketService.delete(id.getId(), user)) {
            return new HttpResponse<>(null);
        } else {
            throw new ShowableException("Ticket not found");
        }
    }
}
