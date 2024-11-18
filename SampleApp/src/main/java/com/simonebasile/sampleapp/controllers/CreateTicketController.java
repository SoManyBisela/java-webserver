package com.simonebasile.sampleapp.controllers;

import com.simonebasile.http.HttpRequest;
import com.simonebasile.http.HttpResponse;
import com.simonebasile.sampleapp.ResponseUtils;
import com.simonebasile.sampleapp.dto.ApplicationRequestContext;
import com.simonebasile.sampleapp.dto.CreateTicket;
import com.simonebasile.sampleapp.handlers.MethodHandler;
import com.simonebasile.sampleapp.mapping.FormHttpMapper;
import com.simonebasile.sampleapp.model.Role;
import com.simonebasile.sampleapp.model.SessionData;
import com.simonebasile.sampleapp.model.Ticket;
import com.simonebasile.sampleapp.model.User;
import com.simonebasile.sampleapp.service.SessionService;
import com.simonebasile.sampleapp.service.TicketService;
import com.simonebasile.sampleapp.service.UserService;
import com.simonebasile.sampleapp.service.errors.CreateTicketException;
import com.simonebasile.sampleapp.views.CreateTicketSection;
import com.simonebasile.sampleapp.views.UserTicketDetailSection;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class CreateTicketController extends MethodHandler<InputStream> {

    private final TicketService ticketService;


    public CreateTicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handleGet(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        //Check role user
        return new HttpResponse<>(r.getVersion(), new CreateTicketSection());
    }

    @Override
    protected HttpResponse<? extends HttpResponse.ResponseBody> handlePost(HttpRequest<? extends InputStream> r, ApplicationRequestContext context) {
        User user = context.getLoggedUser();
        if(user.getRole() != Role.user) {
            log.warn("Unauthorized access to {} {} from user {}", r.getMethod(), r.getResource(), user.getUsername());
            ResponseUtils.redirect(r, "/");
        }
        CreateTicket body = FormHttpMapper.map(r.getBody(), CreateTicket.class);
        String id;
        try {
            id = ticketService.createTicket(new Ticket(body), user).getId();
        } catch (CreateTicketException e) {
            return new HttpResponse<>(r.getVersion(), new CreateTicketSection(e.getMessage()));
        }
        Ticket t = ticketService.getById(id, user);
        return new HttpResponse<>(r.getVersion(), new UserTicketDetailSection(t));
    }
}
