package com.simonebasile.sampleapp.controller.user;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.IdRequest;
import com.simonebasile.sampleapp.dto.UserUpdateTicket;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.http.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.service.errors.UpdateTicketException;
import com.simonebasile.sampleapp.views.TicketNotFoundSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class UserTicketController extends MethodHandler<InputStream, ApplicationRequestContext> {

    private final TicketService ticketService;

    public UserTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        Ticket ticket = ticketService.getById(id.getId(), user);
        return new HttpResponse<>(new UserTicketDetailSection(ticket));
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        CreateTicket body = FormHttpMapper.map(r.getBody(), CreateTicket.class);
        Ticket ticket = new Ticket(body);
        String id;
        try {
            id = ticketService.createTicket(ticket, user).getId();
        } catch (CreateTicketException e) {
            return new HttpResponse<>(new UserTicketDetailSection(ticket).errorMessage(e.getMessage()));
        }
        Ticket t = ticketService.getById(id, user);
        return new HttpResponse<>(new UserTicketDetailSection(t).successMessage("Ticket saved"));
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePut(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        Ticket ticket;
        UserUpdateTicket body = FormHttpMapper.map(r.getBody(), UserUpdateTicket.class);
        try {
            ticket = ticketService.update(body, user);
        } catch (UpdateTicketException e) {
            ticket = ticketService.getById(body.getId(), user);
            return new HttpResponse<>(new UserTicketDetailSection(ticket).errorMessage(e.getMessage()));
        }
        return new HttpResponse<>(new UserTicketDetailSection(ticket));
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleDelete(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        IdRequest id = FormHttpMapper.mapHttpResource(r.getResource(), IdRequest.class);
        if(ticketService.delete(id.getId(), user)) {
            return new HttpResponse<>(null);
        } else {
            return new HttpResponse<>(new TicketNotFoundSection(id.getId()));
        }
    }
}
